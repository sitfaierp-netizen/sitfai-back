# Módulo: inventory — Dominio + Application + Infrastructure
> **Estado:** 🟢 COMPLETADO Y VALIDADO | **Fecha:** 2026-08-10 | **Revisión:** 4.6.0

---

## Bounded Context

**Nombre:** `inventory`
**Paquete raíz:** `com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory`
**Responsabilidad:** Gestión del stock de productos en Bodegas y Logística Inversa (Cuarentena, Mermas). Es el único Bounded Context
autorizado para registrar y consultar movimientos de inventario y transferencias entre bodegas (BOD-03, BOD-06).

---

## Reglas de Negocio Implementadas

| ID     | Regla                                                              | Implementación                                    |
|--------|--------------------------------------------------------------------|---------------------------------------------------|
| BOD-01 | Una Bodega pertenece a exactamente una Sucursal                   | Campo `SucursalId sucursalId` en `Bodega`         |
| BOD-02 | Código de Bodega único dentro de la Sucursal                      | `BodegaRepository.existeCodigoEnSucursal()`       |
| BOD-03 | Solo la Bodega registra movimientos de stock                      | `Bodega.registrarMovimiento()` — único entry point|
| BOD-04 | Todo movimiento exige un Documento Fuente                         | `DocumentoFuenteId` obligatorio en `MovimientoInventario` |
| BOD-05 | ⚠️ Stock nunca negativo (INVARIANTE)                               | `StockInsuficienteException` en `Bodega.registrarMovimiento()` |
| BOD-06 | Transferencias = dos movimientos (SALIDA + ENTRADA)               | Soportado — cada `Bodega` procesa su movimiento   |
| BOD-07 | Rol BODEGA_OPERATOR o superior                                    | `Api_Tienda` controller auth (Pendiente integrar con Keycloak) |
| BOD-08 | Replenishment (Punto de Reorden)                                  | Emite `PuntoReordenAlcanzadoEvent` si el stock <= umbral |
| MT-01  | `empresa_id` presente en todos los registros                      | `EmpresaId empresaId` en `Bodega`, JPA entities y tablas SQL |
| MT-02  | Ningún endpoint/repo retorna datos de otro tenant                 | Filtro por `empresaId` en todos los métodos de búsqueda |
| AUD-01 | Campos `creadoEn`, `actualizadoEn`                                | En el Agregado `Bodega` y `BodegaJpaEntity`       |
| AUD-03 | Domain Events se acumulan para Event Store                        | `domainEvents` en `Bodega`, drenados por Application |

---

## Integraciones Event-Driven (Coreografía Asíncrona)

1. **Consumo de `PedidoConfirmadoEvent` (Salida por Venta - `Api_Tienda`):**
   - Listener: `PedidoConfirmadoEventListener`
   - Efecto: Registra movimientos de `SALIDA` con documento fuente `PEDIDO`.
2. **Consumo de `OrdenCompraRecibidaEvent` (Putaway / Entrada por Compra - `purchasing`):**
   - Listener: `OrdenCompraRecibidaEventListener`
   - Efecto: Registra movimientos de `ENTRADA` con documento fuente `ORDEN_COMPRA` usando los datos transportados en el evento (Event-Carried State Transfer) sin llamadas sincrónicas de regreso a compras.
3. **Consumo de `VentaRegistradaEvent` (Descuento físico de Stock POS - `pos`):**
   - Listener: `VentaPosEventListener`
   - Efecto: Registra movimientos de `SALIDA` en la bodega principal de la sucursal para sincronizar el stock en tiempo real según las ventas de POS.
4. **Consumo de `EmpresaRegistradaIntegrationEvent` (Saga de Aprovisionamiento - `core_empresa`):**
   - Listener: `EmpresaRegistradaInventoryListener`
   - Efecto: Crea automáticamente una Bodega Principal asociada a la sucursal matriz del nuevo Tenant.
5. **Consumo de `DevolucionRegistradaEvent` (Logística Inversa - `pos`):**
   - Listener: `DevolucionPosEventListener`
   - Efecto: `DevolucionPosEventListener` intercepta `DevolucionRegistradaEvent` del POS, traduciendo las devoluciones en ingresos (`ENTRADA`). Por regla de negocio (CAJ-08), esta mercadería se aísla automáticamente resolviendo o enrutando el stock hacia la **Bodega de Cuarentena** (`sucursalId-CUARENTENA`) para su inspección, evitando reingresos accidentales al stock de venta.

### Orquestación de Transferencias (BOD-06)
- **Logística Inversa (Inspección):** A través del `InspeccionCalidadController` (Zero Trust) y el caso de uso `AprobarCuarentenaUseCase`, se orquesta una transferencia doble (SALIDA de Cuarentena y ENTRADA a Bodega Principal) para retornar productos devueltos que pasaron la prueba de calidad, garantizando que el stock vendible solo cambie mediante inspecciones oficiales.

### Aprovisionamiento / Putaway
- **Recepción de Compras:** Cuando el módulo `purchasing` recibe mercancía física y emite `OrdenCompraRecibidaEvent`, el `OrdenCompraRecibidaEventListener` lo intercepta. Esto orquesta comandos automáticos de `ENTRADA` forzando como documento fuente la Orden de Compra (regla BOD-04). La mercadería se destina de manera predeterminada a la Bodega Matriz del tenant (`empresaId-MATRIZ`), garantizando total aislamiento y automatización SCM.

---

## Estructura Completa del Módulo

```text
com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory/
├── domain/
│   ├── model/
│   │   ├── Bodega.java                    ← Aggregate Root (incluye TipoBodega y lógica puedeVender) ✅
│   │   ├── TipoBodega.java                ← Enum (VENTA, CUARENTENA, MERMA) ✅
│   │   ├── MovimientoInventario.java      ← Entidad ✅
│   │   └── TipoMovimiento.java            ← Enum (ENTRADA, SALIDA) ✅
│   ├── src/main/resources/
│   └── db/migration/
│       ├── V8__init_inventory_schema.sql                  ← Migración Flyway base ✅
│       ├── V9__add_domain_events_inventory.sql            ← Migración Flyway eventos ✅
│       ├── V10__update_bodega_tipo_schema.sql             ← Migración Flyway tipo de bodega ✅
│       └── V11__init_bodega_punto_reorden_schema.sql      ← Migración Flyway (BOD-08) ✅
│   ├── valueobject/
│   │   ├── BodegaId.java                  ← VO (record) ✅
│   │   ├── ProductoId.java                ← VO (record) ✅
│   │   ├── EmpresaId.java                 ← VO (record) — Multitenancy ✅
│   │   ├── SucursalId.java                ← VO (record) — BOD-01 ✅
│   │   ├── DocumentoFuenteId.java         ← VO (record) — BOD-04 ✅
│   │   ├── Cantidad.java                  ← VO (record) — BigDecimal wrapper ✅
│   │   └── PuntoReorden.java              ← VO (record) — Replenishment BOD-08 ✅
│   ├── event/
│   │   ├── DomainEvent.java               ← sealed interface ✅
│   │   ├── MovimientoRegistradoEvent.java ← record inmutable ✅
│   │   ├── StockActualizadoEvent.java     ← record inmutable ✅
│   │   └── PuntoReordenAlcanzadoEvent.java← record inmutable (BOD-08) ✅
│   ├── exception/
│   │   ├── DomainException.java           ← Base abstracta (sin frameworks) ✅
│   │   └── StockInsuficienteException.java← BOD-05 invariante ✅
│   ├── service/
│   │   └── TransferenciaStockDomainService.java ← Domain Service puro (Transferencias BOD-06) ✅
│   └── port/
│       ├── input/
│       │   ├── CrearBodegaUseCase.java    ← Driving Port ✅
│       │   ├── RegistrarMovimientoUseCase.java ← Driving Port ✅
│       │   ├── ConsultarStockUseCase.java ← Driving Port (lectura) ✅
│       │   ├── TransferirStockUseCase.java← Driving Port (Transferencias) ✅
│       │   └── ConfigurarPuntoReordenUseCase.java ← Driving Port (BOD-08) ✅
│       └── output/
│           ├── BodegaRepository.java      ← Driven Port (interface pura) ✅
│           └── BodegaEventPublisher.java  ← Driven Port (publicación de eventos) ✅
├── application/
│   ├── dto/
│   │   ├── CrearBodegaCommand.java        ← Command (record) ✅
│   │   ├── RegistrarMovimientoCommand.java← Command (record) ✅
│   │   ├── TransferirStockCommand.java    ← Command (record) ✅
│   │   ├── ConfigurarPuntoReordenCommand.java ← Command (record) ✅
│   │   ├── ConsultarStockQuery.java       ← Query (record) ✅
│   │   ├── BodegaResponse.java            ← Response DTO (record) ✅
│   │   ├── MovimientoResponse.java        ← Response DTO (record) ✅
│   │   └── StockResponse.java             ← Response DTO (record) ✅
│   ├── mapper/
│   │   └── InventarioApplicationMapper.java ← Dominio → DTOs ✅
│   └── service/
│       ├── CrearBodegaService.java        ← @Service, @Transactional ✅
│       ├── RegistrarMovimientoService.java← @Service, @Transactional ✅
│       ├── ConsultarStockService.java     ← @Service, readOnly=true ✅
│       ├── TransferirStockService.java    ← @Service, @Transactional (Orquestación Transferencias) ✅
│       └── ConfigurarPuntoReordenService.java ← @Service, @Transactional (Orquestación BOD-08) ✅
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── event/
    │   │   │   ├── PedidoConfirmadoEventListener.java     ← @EventListener (Salida Pedidos) ✅
    │   │   │   └── OrdenCompraRecibidaEventListener.java  ← @EventListener (Putaway Compras - BOD-03, BOD-04) ✅
    │   │   └── web/
    │   │       ├── BodegaController.java          ← REST Controller Zero Trust ✅
    │   │       ├── TransferenciaController.java   ← REST Controller Zero Trust ✅
    │   │       ├── ReplenishmentController.java   ← REST Controller Zero Trust (BOD-08) ✅
    │   │       ├── InspeccionCalidadController.java ← REST Controller Zero Trust (Inspección Cuarentena) ✅
    │   │       ├── InventoryExceptionHandler.java ← Manejador RFC 7807 ✅
    │   │       └── dto/
    │   │           ├── BodegaWebResponse.java     ← Web DTO ✅
    │   │           ├── CrearBodegaWebRequest.java ← Web DTO ✅
    │   │           ├── MovimientoWebResponse.java ← Web DTO ✅
    │   │           ├── RegistrarMovimientoWebRequest.java ← Web DTO ✅
    │   │           ├── TransferirStockWebRequest.java ← Web DTO ✅
    │   │           ├── ConfigurarPuntoReordenWebRequest.java ← Web DTO (BOD-08) ✅
    │   │           └── StockWebResponse.java      ← Web DTO ✅
    │   └── out/
    │       └── persistence/
    │           ├── entity/
    │           │   ├── BodegaJpaEntity.java              ← @Entity inventory_bodega ✅
    │           │   └── MovimientoInventarioJpaEntity.java← @Entity inventory_movimiento ✅
    │           ├── repository/
    │           │   └── BodegaJpaRepository.java          ← Spring Data JpaRepository ✅
    │           └── adapter/
    │               └── BodegaJpaAdapter.java             ← @Repository (BodegaRepository impl) ✅
    └── mapper/
        ├── BodegaPersistenceMapper.java                  ← Dominio ↔ JPA Entity ✅
        └── InventarioWebMapper.java                      ← Web DTOs ↔ Application DTOs ✅
```

---

## Modelos de Lectura (Read Models - CQRS)

Para evitar bloqueos transaccionales y mejorar el rendimiento de los reportes, se implementó el patrón CQRS para proyecciones de lectura:

1. **Proyección: Stock Consolidado**
   - **Entity:** `StockProyeccionJpaEntity` (Mapea a la tabla/vista `inventory_stock_view`).
   - **Projector:** `StockConsolidadoProjector` (Actualiza asíncronamente en respuesta a `StockActualizadoEvent`).
   - **Query/View:** `ConsultarStockConsolidadoQuery` y `StockConsolidadoView`.
   - **REST Controller:** `InventoryReportController` (GET `/api/v1/inventory/reports/stock-consolidado`).
   - **Seguridad (MT-01, MT-04):** Aislamiento estricto forzado mediante la inyección del `empresa_id` desde el token JWT en la capa del controlador.

---

## Esquema de Base de Datos (Flyway: `V1__init_inventory_schema.sql` y `V10__update_bodega_tipo_schema.sql`)

- `inventory_bodega`: Tabla principal del agregado (UUID, multitenant `empresa_id`, `sucursal_id`, `codigo`, `nombre`, `activa`, timestamps).
- `inventory_bodega_stock`: ElementCollection relacional de stock por producto (`bodega_id`, `producto_id`, `cantidad`).
- `inventory_movimiento`: Historial inmutable de movimientos (`id`, `bodega_id`, `producto_id`, `empresa_id`, `cantidad`, `tipo`, `doc_fuente_tipo`, `doc_fuente_numero`, `fecha_registro`).

---

## Estado de Capas

| Capa | Estado |
|---|---|
| **Domain** | ✅ APROBADO |
| **Application** | ✅ APROBADO |
| **Infrastructure** | ✅ APROBADO |
| **Testing Suite** | ✅ Completo — Tests de unidad e integración pasando |

---

## Suite de Pruebas Automatizadas

```text
src/test/java/com/SITFAI_CORE_ERP_TIENDA/Api_Tienda/inventory/
├── domain/
│   ├── BodegaTest.java                                 ← JUnit 5 puro (BOD-01, BOD-02, BOD-05, DomainEvents) ✅
│   ├── ValueObjectsTest.java                           ← JUnit 5 puro (Cantidad, DocumentoFuenteId, VOs UUID) ✅
│   └── service/
│       └── TransferenciaStockDomainServiceTest.java    ← JUnit 5 puro (Transferencias, BOD-06, BOD-05, MT-01) ✅
├── application/
│   ├── RegistrarMovimientoServiceTest.java             ← JUnit 5 + Mockito (Orquestación, MT-01, MT-02, BOD-05) ✅
│   ├── TransferirStockServiceTest.java                 ← JUnit 5 + Mockito (Orquestación, MT-01, BOD-05, Eventos) ✅
│   └── ConfigurarPuntoReordenServiceTest.java          ← JUnit 5 + Mockito (Orquestación, MT-01) ✅
└── infrastructure/
    ├── adapter/
    │   └── in/
    │       └── event/
    │           ├── PedidoConfirmadoEventListenerTest.java     ← JUnit 5 + Mockito (Salidas por Pedido) ✅
    │           └── OrdenCompraRecibidaEventListenerTest.java  ← JUnit 5 + Mockito (Putaway Compras BOD-03, BOD-04) ✅
    └── persistence/
        └── BodegaJpaRepositoryIT.java                  ← @SpringBootTest + Testcontainers ✅
```
