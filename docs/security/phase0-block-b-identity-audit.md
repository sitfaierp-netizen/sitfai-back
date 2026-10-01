# Phase 0 / Block B — Identity and authorization audit

Date: 2026-10-01  
Base: `develop@d32f4f4ef13aa3f86fc4b1e45d8474916786c994`  
Branch: `fix/phase0-identity-keycloak-hardening`

## Result

`BLOCK_B_IDENTITY_HARDENING_IMPLEMENTED`

The prior onboarding path created a Keycloak credential with `password=username`, authenticated the backend with a human administrator account, swallowed synchronization failures and committed the local user before external identity reconciliation. That combination could leave an active local user without a usable or correctly scoped external identity.

The new path is synchronous and fail-closed:

- the backend authenticates as confidential client `sitfai-backend-admin` with `client_credentials`;
- its secret is mandatory and supplied only through `KEYCLOAK_ADMIN_CLIENT_SECRET`;
- the service account has only `manage-users`, `view-users`, `query-users` and `view-realm`;
- no password is accepted, generated, persisted, logged or returned during provisioning;
- a new identity receives the `UPDATE_PASSWORD` required action;
- Keycloak sends that account action through its configured SMTP channel; the real E2E verifies delivery with Mailpit;
- `empresa_id` and `sitfai_usuario_id` are managed Keycloak attributes and establish ownership;
- username, email and ownership conflicts fail with sanitized HTTP 409;
- Keycloak availability/rejection failures fail the local transaction with sanitized, retryable HTTP 503;
- role and active-state transitions reconcile Keycloak before the local save;
- `PATCH /iam/usuarios/{id}/reconciliar-identidad` provides an idempotent recovery operation;
- only the five SITFAI realm roles are converted into Spring `ROLE_*` authorities;
- method authorization failures preserve HTTP 403 instead of being masked as HTTP 500;
- error bodies and production server settings do not expose exception messages or stack traces.

## Recovery and consistency contract

The operation deliberately does not claim a distributed ACID transaction. Keycloak reconciliation occurs before the local commit. If Keycloak is unavailable, the local transaction fails. If Keycloak succeeds but the HTTP response or local commit is lost, the next identical request finds the identity by exact username, verifies both ownership attributes and reconciles profile, state and the exact managed role. Duplicate identities are therefore not created.

The recovery endpoint applies the same algorithm to an already persisted user. It can be invoked repeatedly and returns the current local user when reconciliation succeeds.

Account-action delivery is intentionally at-least-once: if Keycloak completed provisioning but the local commit or response was lost, a safe retry can send another `UPDATE_PASSWORD` message while the action remains pending. It does not create another identity or credential.

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

Application and domain packages contain no Keycloak imports. The Keycloak SDK is confined to the outbound infrastructure adapter.

## Operational requirements

Production must provide `KEYCLOAK_BOOTSTRAP_ADMIN_USERNAME`, `KEYCLOAK_BOOTSTRAP_ADMIN_PASSWORD` and `KEYCLOAK_ADMIN_CLIENT_SECRET` through the deployment secret store. Bootstrap credentials are only for initial Keycloak installation and are not exposed to the backend. Keycloak 26.0.0 also requires `-Dkeycloak.migration.replace-placeholders=true` during realm import so the confidential-client secret placeholder is resolved.
