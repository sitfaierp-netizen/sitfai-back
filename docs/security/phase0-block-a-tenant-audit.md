# Phase 0 / Block A — Tenant input audit

Audit base: `c6c217d3c453d8e750f5cae1191b78f4019ce057`

## Security decision

The signed JWT claim `empresa_id` (with `empresaId` retained only as a compatibility alias) is the source of truth. `SpringSecurityCurrentTenantProvider` is the canonical adapter. Module-specific ports delegate to it so application and domain code remain independent of HTTP, JWT, and Spring Security.

A normal tenant may operate only on its signed tenant. `ROLE_SUPER_ADMIN` is the only global role and may select another tenant only through an explicit tenant-bearing operation. A tenant mismatch is returned as `404` to avoid confirming cross-tenant resource existence; an authenticated user without the required business role receives `403`.

`X-Empresa-Id` is not authoritative. If absent, the signed claim is used. If present, it must equal the signed claim unless the principal has `ROLE_SUPER_ADMIN`. Missing or malformed trusted tenant context fails closed.

## Scan inventory

The production source scan found 2,136 `empresaId` tokens, 143 `empresa_id` tokens, two textual `X-Empresa-Id` references (the annotation documentation and canonical resolver), two IAM `empresaId` query parameters, no tenant request-header binding, and no legacy `@RequestAttribute("TenantId")` binding. Persistence/domain occurrences that merely carry the already-authorized tenant are `NOT_APPLICABLE` as client authorization inputs.

The 67 REST mappings were reviewed individually:

| # | Route | Effective tenant source | Classification |
|---:|---|---|---|
| 1 | `POST /pedidos` | canonical provider in application | SAFE |
| 2 | `PATCH /pedidos/{id}/confirmar` | canonical provider + scoped aggregate | SAFE |
| 3 | `POST /inventory/bodegas/{id}/analisis-abc` | canonical provider + scoped repository | SAFE |
| 4 | `POST /bodegas` | `@TenantId` + owned Sucursal validation | SAFE |
| 5 | `POST /bodegas/{id}/movimientos` | `@TenantId` + owned Bodega/Product validation | SAFE |
| 6 | `GET /bodegas` | `@TenantId` + tenant query | SAFE |
| 7 | `PATCH /inventory/conteos/{id}/fisico` | canonical provider + scoped aggregate | SAFE |
| 8 | `POST /inventory/conteos/{id}/finalizar` | canonical provider + scoped aggregate | SAFE |
| 9 | `POST /inventory/despachos` | canonical provider in application | SAFE |
| 10 | `POST /inventory/cuarentena/aprobar` | `@TenantId` | SAFE |
| 11 | `POST /inventory/bodegas/{bodegaId}/ingresos` | canonical provider + scoped Bodega | SAFE |
| 12 | `POST /inventory/bodegas/{bodegaId}/egresos` | canonical provider + scoped Bodega | SAFE |
| 13 | `GET /inventory/bodegas/{bodegaId}/stock` | canonical provider + tenant query | SAFE |
| 14 | `GET /inventory/bodegas/{bodegaId}/productos/{productoId}/kardex` | canonical provider + tenant query | SAFE |
| 15 | `POST /inventory/bodegas/{id}/recepciones` | canonical provider + scoped Bodega | SAFE |
| 16 | `PATCH /inventory/recepciones/{id}/productos` | canonical provider + scoped aggregate | SAFE |
| 17 | `POST /inventory/recepciones/{id}/completar` | canonical provider + scoped aggregate | SAFE |
| 18 | `PATCH /inventory/bodegas/{id}/punto-reorden` | `@TenantId` + scoped aggregate | SAFE |
| 19 | `GET /sucursales/{sucursalId}/bodegas` | `@TenantId` + owned Sucursal + tenant query | SAFE |
| 20 | `POST /inventory/transferencias` | `@TenantId` | SAFE |
| 21 | `POST /inventory/ajustes` | canonical provider | SAFE |
| 22 | `POST /production/bom` | canonical provider | SAFE |
| 23 | `PATCH /production/bom/{id}/componentes` | canonical provider + scoped aggregate | SAFE |
| 24 | `POST /production/bom/{id}/aprobar` | canonical provider + scoped aggregate | SAFE |
| 25 | `POST /production/orders` | canonical provider | SAFE |
| 26 | `POST /production/orders/{id}/iniciar` | canonical provider + scoped aggregate | SAFE |
| 27 | `POST /production/orders/{id}/completar` | canonical provider + scoped aggregate | SAFE |
| 28 | `POST /billing/facturas` | `@TenantId` | SAFE |
| 29 | `GET /catalog/productos` | canonical provider + tenant query | SAFE |
| 30 | `POST /catalog/productos` | canonical provider | SAFE |
| 31 | `PATCH /catalog/productos/{productoId}/estado` | canonical provider + scoped aggregate | SAFE |
| 32 | `POST /empresas` | `ROLE_SUPER_ADMIN` only; creates tenant | GLOBAL_ADMIN_REQUIRED |
| 33 | `PUT /empresas/{empresaId}` | `ROLE_SUPER_ADMIN` only | GLOBAL_ADMIN_REQUIRED |
| 34 | `DELETE /empresas/{empresaId}` | `ROLE_SUPER_ADMIN` only | GLOBAL_ADMIN_REQUIRED |
| 35 | `GET /empresas` | `ROLE_SUPER_ADMIN` only | GLOBAL_ADMIN_REQUIRED |
| 36 | `GET /empresas/{empresaId}/sucursales` | authorized path tenant; cross-tenant only global | SAFE / GLOBAL_ADMIN_REQUIRED |
| 37 | `PATCH /empresas/{empresaId}/sucursales/{sucursalId}/estado` | authorized path tenant + scoped aggregate | SAFE / GLOBAL_ADMIN_REQUIRED |
| 38 | `POST /empresas/{empresaId}/sucursales` | authorized path tenant | SAFE / GLOBAL_ADMIN_REQUIRED |
| 39 | `PUT /empresas/{empresaId}/sucursales/{sucursalId}` | authorized path tenant + scoped aggregate | SAFE / GLOBAL_ADMIN_REQUIRED |
| 40 | `DELETE /empresas/{empresaId}/sucursales/{sucursalId}` | authorized path tenant + scoped aggregate | SAFE / GLOBAL_ADMIN_REQUIRED |
| 41 | `POST /fulfillment/despachos/{id}/picking` | canonical provider + scoped aggregate | SAFE |
| 42 | `POST /fulfillment/despachos/{id}/empacar` | canonical provider + scoped aggregate | SAFE |
| 43 | `POST /fulfillment/despachos/{id}/confirmar` | canonical provider + scoped aggregate | SAFE |
| 44 | `POST /iam/usuarios` | request tenant validated against signed tenant | SAFE / GLOBAL_ADMIN_REQUIRED |
| 45 | `PATCH /iam/usuarios/{id}/desactivar` | request tenant validated against signed tenant | SAFE / GLOBAL_ADMIN_REQUIRED |
| 46 | `PATCH /iam/usuarios/{id}/reactivar` | request tenant validated against signed tenant | SAFE / GLOBAL_ADMIN_REQUIRED |
| 47 | `PATCH /iam/usuarios/{id}/rol` | request tenant validated against signed tenant | SAFE / GLOBAL_ADMIN_REQUIRED |
| 48 | `GET /iam/usuarios/{id}?empresaId=...` | query tenant validated against signed tenant | SAFE / GLOBAL_ADMIN_REQUIRED |
| 49 | `GET /iam/usuarios?empresaId=...` | query tenant validated against signed tenant | SAFE / GLOBAL_ADMIN_REQUIRED |
| 50 | `POST /orders/pedidos` | `@TenantId` | SAFE |
| 51 | `PATCH /orders/pedidos/{id}/confirmar` | `@TenantId` + scoped aggregate | SAFE |
| 52 | `GET /cajas` | `@TenantId` + tenant query | SAFE |
| 53 | `POST /pos/turnos/{turnoId}/devoluciones` | canonical provider + scoped aggregate | SAFE |
| 54 | `POST /pos/turnos/abrir` | canonical provider | SAFE |
| 55 | `POST /pos/turnos/{id}/cerrar` | canonical provider + scoped aggregate | SAFE |
| 56 | `POST /pos/turnos/{id}/transacciones` | canonical provider + scoped aggregate | SAFE |
| 57 | `POST /purchasing/ordenes` | canonical provider | SAFE |
| 58 | `POST /purchasing/ordenes/borrador` | `@TenantId` | SAFE |
| 59 | `POST /purchasing/ordenes/{id}/lineas` | `@TenantId` + scoped aggregate | SAFE |
| 60 | `PATCH /purchasing/ordenes/{id}/estado` | `@TenantId` + scoped aggregate | SAFE |
| 61 | `POST /purchasing/solicitudes` | `@TenantId` | SAFE |
| 62 | `PATCH /purchasing/solicitudes/{id}/aprobar` | `@TenantId` + scoped aggregate | SAFE |
| 63 | `POST /api/v1/replenishment/politicas` | `@TenantId` | SAFE |
| 64 | `PUT /api/v1/replenishment/politicas/{id}` | `@TenantId` + composite lookup | SAFE |
| 65 | `POST /returns/rma` | canonical provider | SAFE |
| 66 | `POST /returns/rma/{id}/inspeccionar` | canonical provider + scoped aggregate | SAFE |
| 67 | `POST /sourcing/proveedores` | canonical provider | SAFE |

## Closed unsafe inputs

1. Bodega trusted a client tenant header: replaced by canonical resolution and ownership checks through controller, application, repository, and relationship validation.
2. IAM accepted arbitrary body/query tenant selection: retained for API compatibility but it is now authorized against the signed tenant; only `ROLE_SUPER_ADMIN` may cross tenants.
3. Sucursal/Bodega queried solely by Sucursal UUID: the chain now proves Sucursal ownership and queries by tenant plus Sucursal.
4. Legacy `@RequestAttribute("TenantId")` bindings in Purchasing and Inventory quality inspection bypassed the canonical resolver: migrated to `@TenantId`.

Known non-endpoint debt: `GlobalDomainEventAuditListener` still uses the zero UUID when an event has no tenant. It is not an HTTP authorization bypass and belongs to the later outbox/audit block; it must not be treated as a valid business tenant.
