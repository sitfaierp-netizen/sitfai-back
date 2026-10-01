# Phase 0 / Block C — Bounded-context boundaries

## Dependency policy

```text
web / messaging adapter -> application input port -> application service
application service -> domain model + domain/application output port
infrastructure output adapter -> output port + external framework
domain -> Java/domain only
```

Cross-context collaboration is allowed through explicit events or translation adapters. Importing another context's value object or security port into a core context is not allowed.

## Collaboration map

| Producer | Consumer | Current contract | Boundary status |
|---|---|---|---|
| Core Empresa | Inventory, POS, IAM | company lifecycle / registration events | Transitional integration event; versioning still required. |
| Ventas legacy / Orders | Inventory | order confirmation/reservation events | Two producer models remain a migration risk. |
| Inventory | Replenishment | stock decrease/reorder facts | Valid cross-context collaboration, currently concrete Spring event. |
| Replenishment | Purchasing | `NecesidadAbastecimientoDetectadaEvent` | Valid workflow; durable/versioned delivery deferred. |
| Purchasing | Inventory | purchase-order receipt events | Valid business handoff; contract remains in-process. |
| POS | Inventory, Billing | sale/refund events | Valid handoff, but no durable broker contract yet. |
| Fulfillment | Inventory | canonical `DespachoCompletadoEvent` | One producer aggregate after Block C. |
| Returns | Inventory, Billing | inspection/disposition events | Valid handoff; saga durability remains open. |
| Production | Inventory | material/finished-goods effects | Boundary visible; costing and durable contracts remain open. |
| All contexts | Core Audit/Shared Audit | domain/application events | Two audit stores remain a future consolidation item, not changed in Block C. |

## Persistence ownership and duplicate-table status

| Table/family | Owner | Status |
|---|---|---|
| `tienda_pedido`, `tienda_linea_pedido` | Ventas legacy | `ACTIVE / LEGACY`; duplicate V2/V50 creation is a `MIGRATION_ARTIFACT`. |
| `orders_pedido`, `orders_linea_pedido` | Orders | `ACTIVE / CANONICAL_TARGET`. |
| `billing_factura`, `billing_linea_factura` | Billing | `ACTIVE`; currently shared by canonical and legacy mappers, migration required. |
| `fulfillment_orden_despacho`, `fulfillment_linea_despacho` | Fulfillment | `ACTIVE / CANONICAL`. No table was dropped. |
| `inventory_despacho`, `inventory_linea_despacho` | Inventory/WMS | `CONTEXT_SPECIFIC`; outbound stock operation, not a duplicate Fulfillment aggregate. |
| `purchasing_orden_compra`, line tables | Purchasing | `ACTIVE`; parallel domain models still require reconciliation. |
| `core_idempotency_record` | Core Idempotency | `ACTIVE`; domain ownership no longer leaks to Inventory. |
| `core_audit_event_store` | Core Audit | `ACTIVE`; canonical dispatcher port. |
| `audit_domain_events` | Shared Audit | `ACTIVE`; separate audit semantics remain explicitly documented. |

## Boundary translations

- Tenant: `CurrentTenantProvider` returns an authenticated UUID. Each context adapter translates it into its own `EmpresaId`.
- Identity: actor IDs are obtained through application/domain output ports, never by application services reading Spring Security details.
- Idempotency: the HTTP aspect depends on `core.idempotency.application.port.output.CurrentTenantPort`; its infrastructure adapter delegates to shared security.
- Replenishment: HTTP DTO -> application command -> use case -> domain -> repository port -> JPA adapter.
- Events: domain aggregates collect pure Java event objects; application services publish them through framework adapters.

## Canonical ownership rules

1. Orders is the target owner of the sales-order lifecycle; Tienda sales remains a named legacy context until migrated.
2. Billing's nested `factura` aggregate owns commercial invoice emission; electronic fiscal documents own signing/CUFE behavior.
3. Fulfillment's nested `despacho` package is the only shipment aggregate family.
4. Inventory owns physical stock and WMS dispatch, not customer shipment state.
5. Purchasing's nested `ordencompra` aggregate is the target source for purchase-order evolution.
6. Core modules may not import business-context types.

## Extraction readiness impact

Block C improves package portability for Production, Inventory, Purchasing, Replenishment, Fulfillment, Core Audit and Core Idempotency. It does not make the system microservice-ready: shared MySQL, concrete Java integration events, non-versioned contracts, dual order/invoice/purchase-order models and audit/outbox durability remain blockers.
