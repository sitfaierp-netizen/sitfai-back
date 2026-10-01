# Phase 0 / Block C — Domain map

## Scope and baseline

- Base branch: `develop`
- Base SHA: `621ebb9b99a88ceb19e3a58626899180be69c7ad`
- Work branch: `fix/phase0-domain-consolidation`
- Baseline: 354 Surefire + 42 Failsafe = 396 tests, 0 failures, 0 errors, 4 skipped.
- Audit inventory: 17 backend bounded contexts/capabilities plus the gateway, 31 aggregate roots or persisted candidates, and 67 duplicated simple names.

Block C establishes ownership and dependency direction. It does not add ERP capabilities, split microservices, migrate historical data, or delete tables.

## Bounded contexts and ownership

| Context | Owned domain | Persistence/API ownership | Block C decision |
|---|---|---|---|
| Core Empresa | Empresa, Sucursal | `core_empresa_*`, company APIs | Independent source for company lifecycle. |
| IAM | Usuario, roles, provisioning state | IAM tables, `/api/v1/usuarios`, Keycloak adapter | Identity boundary preserved from Block B. |
| Catalog | Producto, Categoria | catalog tables/APIs | Product identity is context-local; no shared mega-model. |
| Sourcing | Proveedor | sourcing tables/APIs | Supplier vocabulary stays local. |
| Ventas legacy (`Api_Tienda`) | legacy Pedido and reservation saga | `tienda_pedido`, `tienda_linea_pedido`, `/pedidos` | `LEGACY` but active; migration deferred. |
| Orders | transactional Pedido | `orders_pedido`, `orders_linea_pedido`, `/orders/pedidos` | `CANONICAL` target for sales orders. |
| Inventory/WMS | Bodega, stock, receipt, inventory dispatch, count, classification | `inventory_*`, inventory APIs | WMS dispatch is context-specific and is not Fulfillment shipment. |
| Purchasing | Solicitud and purchase orders | `purchasing_*`, purchasing APIs | `ordencompra.OrdenCompra` is canonical target; parallel legacy model remains deferred. |
| Replenishment | PoliticaInventario | `replenishment_politica_inventario`, `/api/v1/replenishment/politicas` | Web now calls an application input port; repository stays behind the use case. |
| Billing | commercial invoice, fiscal electronic invoice, credit note | `billing_*`, `/billing/facturas` | `model.factura.Factura` is canonical commercial invoice; fiscal models remain context-specific. |
| POS | TicketVenta, TurnoCaja | `pos_*`, POS APIs | Independent point-of-sale lifecycle. |
| Fulfillment | shipment order | `fulfillment_orden_despacho`, `fulfillment_linea_despacho`, `/fulfillment/despachos` | `model.despacho.OrdenDespacho` is the sole canonical aggregate family. |
| Returns/RMA | AutorizacionDevolucion | returns tables/APIs | RMA owns inspection/disposition vocabulary. |
| Production | ListaMateriales, OrdenProduccion | production tables/APIs | Domain events are now collected without Spring Data inheritance. |
| Core Audit/Event Store | StoredDomainEvent and outbox dispatch | `core_audit_event_store` | One canonical domain `EventDispatcherPort`. |
| Shared Audit | audit metadata/listeners | `audit_domain_events` and shared audit adapters | Cross-cutting technical concern; not a business aggregate. |
| Core Idempotency/Documents | IdempotencyRecord, transactional document abstraction | `core_idempotency_record` | Owns its tenant value object and port; no Inventory dependency. |
| Gateway | routing/OAuth perimeter | gateway process | Outside backend module CI; no Block C extraction. |

## Canonical aggregate decisions

| Concept | Canonical model | Other model | Status |
|---|---|---|---|
| Sales order | `orders.domain.model.Pedido` | `Api_Tienda.domain.model.pedido.Pedido` | Canonical target selected; active legacy API/table retained pending explicit data and consumer migration. |
| Commercial invoice | `billing.domain.model.factura.Factura` | `billing.domain.model.Factura` | Canonical target selected; legacy readers/services retained until contract migration. |
| Electronic fiscal document | `billing.domain.model.FacturaElectronica` | commercial invoice aggregate | `VALID_BOUNDED_CONTEXT_DUPLICATION`: DIAN signing/CUFE lifecycle is not the same responsibility. |
| Fulfillment shipment | `fulfillment.domain.model.despacho.OrdenDespacho` | former `fulfillment.domain.model.OrdenDespacho` | Consolidated. The unused parallel family and its ports/events were removed. |
| Purchase order | `purchasing.domain.model.ordencompra.OrdenCompra` | `purchasing.domain.model.OrdenCompra` | Canonical target selected; active compatibility path retained pending migration. |

## Critical business flow map

```text
Sales order (legacy or Orders)
  -> stock reservation / movement (Inventory)
  -> fulfillment shipment (Fulfillment canonical OrdenDespacho)
  -> inventory outbound confirmation (Inventory/WMS)
  -> commercial invoice (Billing canonical Factura)
  -> fiscal signing / credit note when applicable (Billing fiscal models)
  -> audit/outbox record (Core Audit)
```

The two sales-order entry paths still prevent a single runtime source of truth. Block C makes the target explicit but does not pretend the required data/API/event migration has already happened.

## Event catalogue

Classification is by semantic role, not merely by a class suffix.

| Category | Contracts/families | Rule |
|---|---|---|
| Domain events | Company/Sucursal lifecycle; Pedido; Inventory stock/reservation/receipt; Purchasing; Billing; POS; Fulfillment; Returns; Production; Catalog/Sourcing/IAM | Owned by the producing context and raised by domain behavior. Context-local `DomainEvent` interfaces are valid duplication. |
| Application events | use-case notifications delivered inside the monolith, including audit dispatch work | May coordinate local application behavior but must not be treated as a durable external contract. |
| Integration events | `EmpresaRegistradaIntegrationEvent` and cross-context uses of stock, order, replenishment, POS and returns events | Current Spring/Java contracts are transitional. Before service extraction they require versioned envelopes, idempotent consumers, correlation/causation and durable delivery. |
| Framework events | Spring `ApplicationEventPublisher` payloads such as `OutboxMessageEvent` | Must remain in adapters/infrastructure and must not leak into domain models. |

The dead Fulfillment `PackingCompletadoEvent` and its private `DomainEvent` contract were removed with the unused aggregate family. `DespachoCompletadoEvent` remains the canonical Fulfillment domain event.

## Shared-kernel policy

- Keep context-specific `EmpresaId`, `ProductoId`, `PedidoId` and `Dinero` when they protect local invariants.
- Use `shared.application.security.CurrentTenantProvider` only as the authenticated tenant source; adapters translate its UUID into local value objects.
- Keep `DocumentoTransaccional` as an existing shared behavioral abstraction, without expanding it into a universal business model.
- Do not introduce shared domain entities merely to reduce repeated class names.

## Deferred migration gates

1. Sales order: inventory every API, table, event producer/consumer and historical row before migrating `tienda_*` into Orders.
2. Billing: migrate legacy readers/credit-note dependencies to the canonical Factura without changing fiscal behavior.
3. Purchasing: reconcile the two state machines and consumers before removing the legacy purchase order.
4. Integration events: define a versioned envelope and transactional ownership before any microservice extraction.
