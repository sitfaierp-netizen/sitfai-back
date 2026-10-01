# Phase 0 / Block B — Keycloak identity flow

## Provisioning

```text
Authorized caller
  -> POST /iam/usuarios
  -> validate tenant, uniqueness and domain invariants
  -> persist local user as PENDIENTE_IDENTIDAD (independent transaction)
  -> IdentityProvisioningPort
  -> Keycloak service account (client_credentials, bounded timeouts)
       -> exact username lookup
       -> create or verify empresa_id + sitfai_usuario_id ownership
       -> set email, disabled state and UPDATE_PASSWORD for a new identity
       -> remove prior SITFAI-managed realm roles
       -> assign the one requested SITFAI role
       -> do not send onboarding yet
  -> commit local user as ACTIVO (independent transaction)
  -> publish local registration event
  -> enable the Keycloak identity
  -> send the UPDATE_PASSWORD account-action email through Keycloak SMTP
  -> HTTP 201
```

No password crosses this flow. Initial password enrollment is a Keycloak user action and must be completed through the configured account-action delivery process. Delivery begins only after the local `ACTIVO` commit and is at-least-once while `UPDATE_PASSWORD` remains pending, so a recovery retry may send a second action message without creating a duplicate identity or credential.

## Failure and retry

```text
Keycloak timeout / transport failure / rejected operation
  -> typed application exception
  -> local user remains PENDIENTE_IDENTIDAD
  -> HTTP 503 IDENTITY_PROVIDER_UNAVAILABLE (retryable=true)
  -> caller retries the same command
  -> pending row is found by tenant + username/email
  -> its server-generated UUID is reused even when request id is null
  -> exact identity ownership check + reconciliation
  -> one external identity, one local user
```

An existing username without the expected tenant ownership is never adopted. It produces `409 IDENTITY_CONFLICT` and requires administrative investigation.

If external preparation succeeds and local activation fails, the Keycloak user remains disabled, no onboarding email is sent and the committed pending row remains the recovery anchor. Retrying the same payload completes activation and onboarding with the same local UUID.

## Role and lifecycle synchronization

Role change, deactivation and reactivation update the domain aggregate, reconcile the corresponding Keycloak role/state, then save locally. A Keycloak failure aborts the local transaction. The explicit reconciliation endpoint repairs a previously interrupted or externally drifted identity without creating another one.

## Token consumption

Keycloak emits realm roles and `empresa_id`. The resource server validates the JWT signature/issuer, maps only the five recognized SITFAI roles, and stores `empresa_id` as tenant authentication details. Method security and `CurrentTenantProvider` then enforce role and tenant scope.

## Secret boundaries

- deployment secret store -> Keycloak realm import placeholder;
- deployment secret store -> backend `KEYCLOAK_ADMIN_CLIENT_SECRET`;
- deployment secret store -> backend and Compose `DB_PASSWORD` (mandatory, without a default);
- backend never receives Keycloak bootstrap administrator credentials;
- logs contain local user and tenant IDs for correlation but no password, client secret, token or provider response body;
- API errors expose stable codes, not upstream exception details.
