# Phase 0 / Block B — Identity and authorization audit

Date: 2026-10-01  
Base: `develop@d32f4f4ef13aa3f86fc4b1e45d8474916786c994`  
Branch: `fix/phase0-identity-keycloak-hardening`

## Result

`BLOCK_B_IDENTITY_HARDENING_IMPLEMENTED`

The prior onboarding path created a Keycloak credential with `password=username`, authenticated the backend with a human administrator account, swallowed synchronization failures and committed the local user before external identity reconciliation. That combination could leave an active local user without a usable or correctly scoped external identity.

The new path is synchronous, staged and fail-closed:

- the backend authenticates as confidential client `sitfai-backend-admin` with `client_credentials`;
- its secret is mandatory and supplied only through `KEYCLOAK_ADMIN_CLIENT_SECRET`;
- the service account has only `manage-users`, `view-users`, `query-users` and `view-realm`;
- no password is accepted, generated, persisted, logged or returned during provisioning;
- a new identity receives the `UPDATE_PASSWORD` required action;
- Keycloak sends that account action through its configured SMTP channel; the real E2E verifies delivery with Mailpit;
- `empresa_id` and `sitfai_usuario_id` are managed Keycloak attributes and establish ownership;
- every new local user is committed first as `PENDIENTE_IDENTIDAD`, in an independent transaction, before an external side effect occurs;
- Keycloak prepares that identity disabled and sends no onboarding message until the local user has committed as `ACTIVO`;
- username, email and ownership conflicts fail with sanitized HTTP 409;
- Keycloak availability/rejection failures fail the local transaction with sanitized, retryable HTTP 503;
- role and active-state transitions reconcile Keycloak before the local save;
- `PATCH /iam/usuarios/{id}/reconciliar-identidad` provides an idempotent recovery operation;
- only the five SITFAI realm roles are converted into Spring `ROLE_*` authorities;
- method authorization failures preserve HTTP 403 instead of being masked as HTTP 500;
- error bodies and production server settings do not expose exception messages or stack traces.
- the production datasource password has no default value; `DB_PASSWORD` is mandatory in both Spring configuration and Docker Compose.

## Recovery and consistency contract

The operation deliberately does not claim a distributed ACID transaction. It uses a recoverable state machine instead:

1. persist the local user as `PENDIENTE_IDENTIDAD` in an independent transaction;
2. create or reconcile a disabled Keycloak identity, without credential or onboarding email;
3. commit the local transition to `ACTIVO` in another independent transaction;
4. enable the Keycloak identity and send the `UPDATE_PASSWORD` account-action email.

If Keycloak is unavailable, the pending local record remains available for retry. If Keycloak succeeds but local activation fails, the external identity remains disabled and no onboarding message is sent. A retry whose request again omits `id` finds the pending row by tenant plus username/email, reuses its server-generated UUID, verifies both ownership attributes and completes the state machine without duplication.

The recovery endpoint applies the same algorithm to an already persisted pending or active user. It can be invoked repeatedly and returns the current local user when reconciliation succeeds.

Account-action delivery is intentionally at-least-once after local consistency has been established. A safe retry can send another `UPDATE_PASSWORD` message while the action remains pending, but never before the local user is `ACTIVO`; it does not create another identity or credential.

## Authorization and tenant boundaries

`SUPER_ADMIN` may act globally. Tenant roles require a valid `empresa_id` claim and may only act on that tenant. Cross-tenant requests return the existing non-disclosing 404 response. Roles outside `SUPER_ADMIN`, `EMPRESA_ADMIN`, `SUCURSAL_MANAGER`, `BODEGA_OPERATOR` and `CAJERO` are ignored by the JWT converter even if present in the realm token.

## Verification evidence

`KeycloakIdentityIntegrationTest` starts MySQL 8.4 and Keycloak 26.0.0 with the production realm import and active Spring Security filters. It verifies:

- service-account provisioning with no initial credential;
- rejection of `password=username`;
- exact tenant and local-user ownership attributes;
- exact realm role mapping;
- tenant isolation and insufficient-role denial;
- deactivation blocks a new login and reactivation restores it;
- repeated reconciliation creates no duplicate;
- a paused Keycloak produces HTTP 503 and a retry after recovery succeeds.
- an injected local activation failure after successful external preparation leaves one persisted pending user and one disabled Keycloak identity, sends no email, and a second request with `id=null` completes using the same UUID.

Application and domain packages contain no Keycloak imports. The Keycloak SDK is confined to the outbound infrastructure adapter.

## Operational requirements

Production must provide `KEYCLOAK_BOOTSTRAP_ADMIN_USERNAME`, `KEYCLOAK_BOOTSTRAP_ADMIN_PASSWORD` and `KEYCLOAK_ADMIN_CLIENT_SECRET` through the deployment secret store. Bootstrap credentials are only for initial Keycloak installation and are not exposed to the backend. Keycloak 26.0.0 also requires `-Dkeycloak.migration.replace-placeholders=true` during realm import so the confidential-client secret placeholder is resolved.

`DB_PASSWORD` is likewise mandatory: there is no production fallback, and Docker Compose refuses to start without it.

## Deferred observability risk

SQL statement logging and JDBC bind-value tracing can expose business or personal data when enabled outside a controlled test environment. Removing and centrally governing `spring.jpa.show-sql`, Hibernate SQL logging and bind-parameter tracing is recorded for the later observability hardening block; no credential or token logging was introduced here.
