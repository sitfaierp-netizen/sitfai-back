# Phase 0 / Block B — Keycloak identity flow

## Provisioning

```text
Authorized caller
  -> POST /iam/usuarios
  -> validate tenant, uniqueness and domain invariants
  -> IdentityProvisioningPort
  -> Keycloak service account (client_credentials, bounded timeouts)
       -> exact username lookup
       -> create or verify empresa_id + sitfai_usuario_id ownership
       -> set email, enabled state and UPDATE_PASSWORD for a new identity
       -> remove prior SITFAI-managed realm roles
       -> assign the one requested SITFAI role
       -> send the UPDATE_PASSWORD account-action email through Keycloak SMTP
  -> save local user
  -> publish local domain events
  -> HTTP 201
```

No password crosses this flow. Initial password enrollment is a Keycloak user action and must be completed through the configured account-action delivery process. Delivery is at-least-once while `UPDATE_PASSWORD` remains pending, so a recovery retry may send a second action message without creating a duplicate identity or credential.

## Failure and retry

```text
Keycloak timeout / transport failure / rejected operation
  -> typed application exception
  -> local transaction rollback
  -> HTTP 503 IDENTITY_PROVIDER_UNAVAILABLE (retryable=true)
  -> caller retries the same command
  -> exact identity ownership check + reconciliation
  -> one external identity, one local user
```

An existing username without the expected tenant ownership is never adopted. It produces `409 IDENTITY_CONFLICT` and requires administrative investigation.

## Role and lifecycle synchronization

Role change, deactivation and reactivation update the domain aggregate, reconcile the corresponding Keycloak role/state, then save locally. A Keycloak failure aborts the local transaction. The explicit reconciliation endpoint repairs a previously interrupted or externally drifted identity without creating another one.

## Token consumption

Keycloak emits realm roles and `empresa_id`. The resource server validates the JWT signature/issuer, maps only the five recognized SITFAI roles, and stores `empresa_id` as tenant authentication details. Method security and `CurrentTenantProvider` then enforce role and tenant scope.

## Secret boundaries

- deployment secret store -> Keycloak realm import placeholder;
- deployment secret store -> backend `KEYCLOAK_ADMIN_CLIENT_SECRET`;
- backend never receives Keycloak bootstrap administrator credentials;
- logs contain local user and tenant IDs for correlation but no password, client secret, token or provider response body;
- API errors expose stable codes, not upstream exception details.
