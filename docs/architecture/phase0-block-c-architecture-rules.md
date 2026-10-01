# Phase 0 / Block C — Architecture rules

## Enforced rules

`ArchitecturalBoundariesTest` is a source-level equivalent to ArchUnit tailored to the current package topology. It adds no runtime dependency and protects only rules already made true in this block.

| Rule | Before Block C | After Block C | Enforcement |
|---|---:|---:|---|
| Domain must not import Spring | 3 classes | 0 | `domainDoesNotImportSpring` |
| Domain must not import infrastructure | 0 observed imports | 0 | `domainDoesNotImportInfrastructure` |
| Domain must not import application | 7 Inventory driving-port interfaces | 0 | `domainDoesNotImportApplication` |
| Application must not import infrastructure | 3 Purchasing services | 0 | `applicationDoesNotImportInfrastructure` |
| Web controllers must not import repositories | 1 Replenishment controller | 0 | `webControllersDoNotImportRepositories` |
| Core Idempotency must not import Inventory | 1 cross-context dependency family across 6 production files | 0 | `coreIdempotencyDoesNotDependOnInventory` |
| Core Audit has one dispatcher port | 2 interfaces | 1 domain output port | `coreAuditHasOneCanonicalEventDispatcherPort` |
| Fulfillment has one shipment aggregate family | 2 families | 1 active family | `fulfillmentKeepsOnlyTheActiveDispatchAggregateFamily` |

Fourteen documented hexagonal dependency violations were removed: three domain-to-Spring classes, seven misplaced domain input ports, three application-to-infrastructure services and one web-to-repository controller.

## Implementation notes

### Pure domain events

`RecepcionMercancia`, `ListaMateriales` and `OrdenProduccion` now use private Java collections for pending events. Existing event classes, publication order and application-service calls remain unchanged. Reconstitution starts with an empty event list.

### Application tenant boundary

Purchasing application services call `CurrentTenantProvider.authorizeTenant(...)`. This removes direct knowledge of `SecurityContextHolder` and `TenantAuthenticationDetails`, while preserving authenticated-tenant/global-admin semantics established in Block A.

### Web boundary

`PoliticaInventarioController` maps HTTP data into `GestionarPoliticaInventarioCommand` and calls `GestionarPoliticaInventarioUseCase`. Construction, tenant-scoped lookup and persistence now live in the application service.

### Core idempotency boundary

Core Idempotency owns `EmpresaId` and `CurrentTenantPort`. `SecurityCurrentTenantAdapter` is the only translation from shared authenticated tenant UUID to that local value object.

### Driving-port placement

Inventory input ports that consume application commands/responses now live under `inventory.application.port.input`. The domain package no longer depends upward on DTOs.

## Deliberate non-rules

- Do not ban equal simple names across bounded contexts.
- Do not require all contexts to share `EmpresaId`, `ProductoId`, `PedidoId`, `Dinero`, tenant ports or actor ports.
- Do not ban cross-context events inside the monolith until versioned integration contracts exist.
- Do not enforce one global repository namespace; ports remain context-owned.
- Do not require a big-bang package rename.

## Verification sequence

1. `test-compile` compiles the complete production and test graph.
2. Targeted unit tests cover pure event collection, idempotency, Purchasing tenant resolution, Replenishment orchestration and all eight architecture rules.
3. Targeted Testcontainers integration covers Idempotency, Inventory receipt, Production BOM/order, Purchasing, Replenishment and canonical Fulfillment.
4. Full `clean verify` is the release gate and includes all Surefire/Failsafe tests, Flyway, JaCoCo and OWASP Dependency-Check.

## Change protocol

Any intentional exception to these boundaries must update this document and the architecture test in the same pull request, with a bounded-context rationale. Disabling a rule merely to land a dependency inversion is not acceptable.
