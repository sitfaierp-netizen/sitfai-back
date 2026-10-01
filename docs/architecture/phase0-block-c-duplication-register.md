# Phase 0 / Block C — Duplication register

## Classification rules

- `VALID_BOUNDED_CONTEXT_DUPLICATION`: same name, different context-owned meaning.
- `CANONICAL`: selected source of truth or target model.
- `LEGACY`: still referenced and therefore not safe to delete without migration.
- `ACCIDENTAL_DUPLICATION`: competing implementation of the same responsibility.
- `ADAPTER_MODEL`: technical representation that must remain outside the domain.
- `SHARED_KERNEL_CANDIDATE`: genuinely universal contract; sharing still requires explicit governance.

The audit's 67 duplicated simple names were screened. Twelve priority families received dependency/runtime tracing.

## Priority register

| Family | Instances/evidence | Classification | Decision |
|---|---|---|---|
| Pedido | `Api_Tienda...pedido.Pedido` -> `tienda_*` and `/pedidos`; `orders...Pedido` -> `orders_*` and `/orders/pedidos` | `ACCIDENTAL_DUPLICATION`, Orders `CANONICAL`, Tienda `LEGACY` | Preserve both runtime paths; defer data/API/event migration. |
| Factura | `billing.domain.model.Factura` and `billing.domain.model.factura.Factura` share `billing_factura` through compatibility mapper/repository | `ACCIDENTAL_DUPLICATION`; nested model `CANONICAL`, root model `LEGACY` | Preserve legacy services until readers and credit-note flow migrate. |
| FacturaElectronica | DIAN resolution, CUFE, signing and fiscal state | `VALID_BOUNDED_CONTEXT_DUPLICATION` | Preserve; it is not a synonym for the commercial invoice aggregate. |
| OrdenCompra | root purchasing model and `model.ordencompra` model have incompatible states | `ACCIDENTAL_DUPLICATION`; nested model `CANONICAL`, root model `LEGACY` | Defer until API/event/data consumers are migrated. |
| OrdenDespacho | root family had no service, controller, persistence adapter, table mapper or test; nested family backs API/JPA/tests | `ACCIDENTAL_DUPLICATION`; nested family `CANONICAL`; root family dead | Removed 15 dead types and the stale exception-handler branch. |
| DomainEvent | context-local interfaces plus concrete cross-context events | `VALID_BOUNDED_CONTEXT_DUPLICATION` locally; integration-envelope gap remains | Preserve local contracts; removed only the dead Fulfillment contract. |
| EmpresaId | local tenant identity VOs across contexts | `VALID_BOUNDED_CONTEXT_DUPLICATION` | Preserve. Core idempotency now owns its own `EmpresaId`. |
| ProductoId | catalog, inventory, purchasing, fulfillment, POS, etc. | `VALID_BOUNDED_CONTEXT_DUPLICATION` | Preserve; translate at boundaries. |
| PedidoId | sales, billing, fulfillment/inventory references | `VALID_BOUNDED_CONTEXT_DUPLICATION` | Preserve local semantics (`PedidoOrigenId` where appropriate). |
| Dinero/Money | context-local money invariants and fiscal calculations | `VALID_BOUNDED_CONTEXT_DUPLICATION` | Preserve until currency/rounding policy is proven universal. |
| TenantProviderPort | context ports return local tenant VOs | `VALID_BOUNDED_CONTEXT_DUPLICATION`; shared authenticated source is a `SHARED_KERNEL_CANDIDATE` already represented by `CurrentTenantProvider` | Keep local adapters; do not import another context's port. |
| CurrentActorProvider | context-specific actor ports and core audit actor contract | `VALID_BOUNDED_CONTEXT_DUPLICATION` | Preserve where return semantics differ. |
| EventDispatcherPort | identical application alias extending domain port in `core_audit` | `ACCIDENTAL_DUPLICATION` | Application alias removed; domain output port is canonical. |
| Repositories for parallel aggregates | Pedido/Factura/OrdenCompra/OrdenDespacho alternatives | `ACCIDENTAL_DUPLICATION` when tied to a competing aggregate; otherwise valid port/adapter separation | Fulfillment dead repository removed; active legacy migrations deferred. |
| JPA/Web DTO classes | persistence entities and HTTP request/response types | `ADAPTER_MODEL` | Preserve outside domain; they are not duplicate aggregates. |

## Consolidated in Block C

1. Fulfillment now has one `OrdenDespacho` aggregate family, one repository contract, one JPA path and one completion event.
2. Core Audit now has one `EventDispatcherPort`, owned by `domain.port.output`.
3. Core Idempotency no longer imports Inventory's `EmpresaId` or `TenantProviderPort`; it owns both its tenant VO and application output port.
4. Seven Inventory driving ports were moved from `domain.port.input` to `application.port.input` so domain no longer imports application DTOs.
5. Three Spring Data aggregate bases were replaced by pure-Java event collections without changing event contracts.
6. Three Purchasing application services now depend on the shared application tenant contract, not Spring Security infrastructure.
7. Replenishment web now calls an application use case rather than a repository.
8. Inventory `PuntoReordenAlcanzadoEvent` now enters Purchasing through a dedicated trusted input port. Authenticated Purchasing commands and internal events share a tenant-agnostic application operation without sharing their trust decision.

## Dead code confirmed and removed

The following unused Fulfillment family had no inbound runtime, persistence implementation, or tests:

- root `OrdenDespacho`, `LineaDespacho`, `EstadoDespacho`;
- six root value objects;
- root `DomainEvent` and `PackingCompletadoEvent`;
- repository and event-publisher ports;
- Spring event-publisher adapter;
- unused `DespachoNoEncontradoException`.

No table or migration was deleted. `fulfillment_orden_despacho` and `fulfillment_linea_despacho` remain owned by the canonical nested aggregate. `inventory_despacho` remains a distinct WMS outbound model.

## Legacy deferred

| Legacy path | Why not deleted | Required evidence before removal |
|---|---|---|
| Tienda Pedido | Active REST path, application services, JPA adapter, saga test and `tienda_*` data | data migration, API compatibility, event consumer migration, rollback plan |
| Root Billing Factura | Active services/repository compatibility and credit-note dependency over shared table | service/consumer migration plus historical reconstitution tests |
| Root Purchasing OrdenCompra | Application services and a distinct state machine still compile as beans | API/consumer inventory and state/data migration |

The point-of-reorder flow still uses `PROVEEDOR_DEFAULT`, a fixed replenishment quantity and a temporary mock unit cost. These are explicit functional debts and are not part of the tenant-boundary correction.

## Counts

- Simple-name collisions screened: 67.
- Priority families traced: 12.
- Valid context-duplication families preserved: 7.
- Accidental duplication families confirmed: 5.
- Accidental duplication families fixed: 2.
- Canonical aggregate targets recorded: 4.
- Active legacy aggregate families deferred: 3.
- Dead Fulfillment production types removed: 15.
- Ports consolidated/removed: 3.
- Event types/contracts consolidated/removed: 2.
