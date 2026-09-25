# Módulo: inventory — Dominio + Application + Infrastructure
> **Estado:** 🟢 COMPLETADO Y VALIDADO | **Fecha:** 2026-09-22 | **Revisión:** 5.0.0 (Logística de Entrada y Salida)

---

## Bounded Context

**Nombre:** `inventory`  
**Paquete raíz:** `com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory`  
**Responsabilidad:** Gestión integral del stock de productos en Bodegas, Logística de Entrada (Inbound Logistics / Recepciones amparadas en Órdenes de Compra), Logística de Salida (Outbound Logistics / Despachos con deducción física de reservas), Transferencias entre bodegas y Logística Inversa (Cuarentena, Mermas).

---

## Reglas de Negocio Implementadas

| ID     | Regla                                                              | Implementación                                    |
|--------|--------------------------------------------------------------------|---------------------------------------------------|
| BOD-01 | Una Bodega pertenece a exactamente una Sucursal                   | Campo `SucursalId sucursalId` en `Bodega`         |
| BOD-02 | Código de Bodega único dentro de la Sucursal                      | `BodegaRepository.existeCodigoEnSucursal()`       |
| BOD-03 | Solo la Bodega registra movimientos de stock                      | `Bodega.registrarMovimiento()` — único entry point|
| BOD-04 | Todo movimiento exige un Documento Fuente                         | `DocumentoFuenteId` obligatorio en `MovimientoInventario` |
| BOD-05 | ⚠️ Stock nunca negativo (INVARIANTE)                               | `StockInsuficienteException` en `Bodega`          |
| BOD-06 | Transferencias = dos movimientos (SALIDA + ENTRADA)               | Soportado — cada `Bodega` procesa su movimiento   |
| BOD-07 | Rol BODEGA_OPERATOR o superior                                    | `Api_Tienda` controller auth y Security Claims    |
| BOD-08 | Replenishment (Punto de Reorden)                                  | Emite `PuntoReordenAlcanzadoEvent` si stock <= umbral |
| MT-01  | `empresa_id` presente en todos los registros                      | `EmpresaId` en `Bodega`, `Despacho`, `Recepcion`, JPA y SQL |
| MT-02  | Ningún endpoint/repo retorna datos de otro tenant                 | Filtro estricto por `empresaId` en repositorios y JWT claims |
| AUD-01 | Auditoría transaccional (`creado_en`, `creado_por`, etc.)          | Herencia de `AuditableJpaEntity` en JPA entities  |
| AUD-03 | Domain Events se acumulan para Event Store                        | `domainEvents` en Agregados, drenados por Application |

---

## Integraciones Event-Driven (Coreografía Asíncrona)

1. **Logística de Entrada (Inbound Logistics):**
   - Agregado: `Recepcion` y endpoint `POST /api/v1/inventory/bodegas/{id}/recepciones`.
   - Evento: `IngresoStockRegistradoEvent`.
   - Efecto: Ingresa físicamente lotes con fecha de caducidad (FEFO) y vincula la Orden de Compra upstream (BOD-04).
2. **Logística de Salida y Cierre de Inventario (Outbound Logistics):**
   - Agregado: `Despacho` y endpoint `POST /api/v1/inventory/despachos`.
   - Evento: `DespachoConfirmadoEvent`.
   - Handler: `DespachoConfirmadoEventHandler`.
   - Efecto: Al confirmar el despacho físico del pedido, ejecuta `bodega.deducirStockReservado(...)`, descontando definitivamente las unidades comprometidas de la reserva física y generando el movimiento de salida.
3. **Consumo de `PedidoConfirmadoEvent` (Salida por Venta - `Api_Tienda`):**
   - Listener: `PedidoConfirmadoEventListener`.
   - Efecto: Registra movimientos de `SALIDA` con documento fuente `PEDIDO`.
4. **Consumo de `DevolucionRegistradaEvent` (Logística Inversa - `pos`):**
   - Listener: `DevolucionPosEventListener`.
   - Efecto: Aísla la mercadería devuelta en la Bodega de Cuarentena para inspección.

---

## Estructura Completa del Módulo

```text
com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory/
├── domain/
│   ├── model/
│   │   ├── Bodega.java                    ← Aggregate Root (Stock, FEFO, deducirStockReservado) ✅
│   │   ├── StockLote.java                 ← Entidad interna (cantidad disponible y reservada) ✅
│   │   ├── TipoBodega.java                ← Enum (VENTA, CUARENTENA, MERMA) ✅
│   │   ├── MovimientoInventario.java      ← Entidad de historial inmutable ✅
│   │   ├── TipoMovimiento.java            ← Enum (ENTRADA, SALIDA) ✅
│   │   ├── despacho/
│   │   │   ├── Despacho.java              ← Aggregate Root (Outbound Logistics) ✅
│   │   │   ├── LineaDespacho.java         ← Entidad interna protegida ✅
│   │   │   └── vo/
│   │   │       ├── DespachoId.java        ← Value Object (UUID) ✅
│   │   │       ├── PedidoId.java          ← Value Object (BOD-04 Pedido origen) ✅
│   │   │       └── EstadoDespacho.java    ← Enum (PENDIENTE, EN_PICKING, EMPACADO, DESPACHADO) ✅
│   │   └── recepcion/
│   │       ├── Recepcion.java             ← Aggregate Root (Inbound Logistics) ✅
│   │       └── LineaRecepcion.java        ← Entidad interna ✅
│   ├── valueobject/
│   │   ├── BodegaId.java                  ← VO (record) ✅
│   │   ├── ProductoId.java                ← VO (record) ✅
│   │   ├── EmpresaId.java                 ← VO (record) — Multitenancy MT-01 ✅
│   │   ├── SucursalId.java                ← VO (record) — BOD-01 ✅
│   │   ├── DocumentoFuenteId.java         ← VO (record) — BOD-04 ✅
│   │   ├── Cantidad.java                  ← VO (record) — BigDecimal wrapper ✅
│   │   └── PuntoReorden.java              ← VO (record) — Replenishment BOD-08 ✅
│   ├── event/
│   │   ├── DomainEvent.java               ← sealed interface ✅
│   │   ├── MovimientoRegistradoEvent.java ← record inmutable ✅
│   │   ├── StockActualizadoEvent.java     ← record inmutable ✅
│   │   ├── StockReservadoEvent.java       ← record inmutable ✅
│   │   ├── IngresoStockRegistradoEvent.java ← record inmutable ✅
│   │   └── DespachoConfirmadoEvent.java   ← record inmutable (Outbound Logistics) ✅
│   └── port/
│       ├── input/
│       │   ├── CrearBodegaUseCase.java    ← Driving Port ✅
│       │   ├── RegistrarMovimientoUseCase.java ← Driving Port ✅
│       │   ├── RecepcionarMercanciaUseCase.java ← Driving Port (Inbound) ✅
│       │   └── ConfirmarDespachoUseCase.java   ← Driving Port (Outbound) ✅
│       └── output/
│           ├── BodegaRepository.java      ← Driven Port (MT-01 en todas las firmas) ✅
│           ├── DespachoRepository.java    ← Driven Port (MT-01 en todas las firmas) ✅
│           └── BodegaEventPublisher.java  ← Driven Port ✅
├── application/
│   ├── dto/
│   │   ├── ConfirmarDespachoCommand.java  ← Command inmutable ✅
│   │   ├── DespachoResponse.java          ← Response DTO ✅
│   │   ├── RecepcionarMercanciaCommand.java ← Command inmutable ✅
│   │   └── RecepcionMercanciaResponse.java  ← Response DTO ✅
│   └── service/
│       ├── ConfirmarDespachoService.java  ← @Service @Transactional (Outbound Logistics) ✅
│       └── RecepcionarMercanciaService.java ← @Service @Transactional (Inbound Logistics) ✅
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── messaging/
    │   │   │   └── DespachoConfirmadoEventHandler.java ← @EventListener Cierre de ciclo / Deducción ✅
    │   │   └── web/
    │   │       ├── DespachoController.java    ← REST Controller POST /api/v1/inventory/despachos ✅
    │   │       ├── RecepcionController.java   ← REST Controller POST /api/v1/inventory/bodegas/{id}/recepciones ✅
    │   │       └── dto/
    │   │           ├── ConfirmarDespachoWebRequest.java ← Web DTO desacoplado ✅
    │   │           └── RecepcionarMercanciaWebRequest.java ← Web DTO desacoplado ✅
    │   └── out/
    │       └── persistence/
    │           ├── entity/
    │           │   ├── DespachoJpaEntity.java       ← @Entity inventory_despacho (AuditableJpaEntity) ✅
    │           │   └── LineaDespachoJpaEntity.java  ← @Entity inventory_linea_despacho (AuditableJpaEntity) ✅
    │           ├── repository/
    │           │   └── DespachoJpaRepository.java   ← Spring Data JPA (MT-01) ✅
    │           ├── adapter/
    │           │   └── DespachoJpaAdapter.java      ← Implementación DespachoRepository ✅
    │           └── mapper/
    │               └── DespachoPersistenceMapper.java ← Mapeo Dominio ↔ JPA Entity ✅
    └── db/migration/
        ├── V36__init_inbound_recepcion.sql          ← Flyway Inbound Logistics ✅
        └── V43__init_outbound_logistics_schema.sql  ← Flyway Outbound Logistics ✅
```

---

## Esquema de Base de Datos (Flyway V43)

- `inventory_despacho`: Tabla del agregado de despacho (`id`, `empresa_id`, `pedido_id`, `bodega_id`, `estado`, `version`, `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por`).
- `inventory_linea_despacho`: Líneas de despacho (`id`, `despacho_id`, `empresa_id`, `producto_id`, `cantidad`, `version`, `creado_en`, `creado_por`, timestamps).
- Índices de partición y concurrencia: `idx_inv_despacho_empresa`, `idx_inv_despacho_pedido`, `idx_inv_linea_despacho_despacho`, `idx_inv_linea_despacho_empresa`.

---

## Estado de Capas y Certificación

| Capa | Estado |
|---|---|
| **Domain** | ✅ APROBADO (Despacho, LineaDespacho, DespachoId, PedidoId, EstadoDespacho, DespachoConfirmadoEvent, ClasificacionProducto, AnalisisId, MetricaMovimiento, CategoriaABC, ProductoReclasificadoEvent) |
| **Application** | ✅ APROBADO (ConfirmarDespachoUseCase, EjecutarAnalisisAbcUseCase, EjecutarAnalisisAbcService, EjecutarAnalisisAbcCommand, AnalisisAbcResponse) |
| **Infrastructure** | ✅ APROBADO (DespachoController, AnalisisAbcController, ClasificacionProductoJpaEntity, ClasificacionProductoJpaAdapter, JdbcMetricaMovimientoQueryAdapter, Flyway V43, V49) |
| **Testing Suite** | ✅ APROBADO (DespachoTest, ClasificacionProductoTest 22/22, OutboundLogisticsIntegrationTest, AnalisisAbcIntegrationTest) |
| **Compilación** | ✅ BUILD SUCCESS (`.\mvnw test-compile`) |

---

## Motor WMS — Análisis ABC de Inventario (Ley de Pareto)

### Sub-Bounded Context: `inventory/clasificacion`
- **Agregado:** `ClasificacionProducto`
- **Value Objects:** `AnalisisId`, `BodegaId`, `ProductoId`, `EmpresaId`, `MetricaMovimiento` (con validación fail-fast y DECIMAL(19,4)), `CategoriaABC` (A, B, C, NO_CLASIFICADO).
- **Domain Event:** `ProductoReclasificadoEvent` (disparado condicionalmente al cambiar de categoría).
- **Output Port:** `ClasificacionProductoRepository` (persistencia pura) y `MetricaMovimientoQueryPort` (lectura de métricas históricas).
- **Application Service:** `EjecutarAnalisisAbcService` (orquesta cálculo de Pareto: A top 80%, B siguiente 15%, C último 5%).
- **Driving Adapter:** `AnalisisAbcController` (`POST /inventory/bodegas/{id}/analisis-abc`).
- **Driven Adapter:** `ClasificacionProductoJpaAdapter` con `SpringDataClasificacionProductoRepository`.
- **Query Adapter:** `JdbcMetricaMovimientoQueryAdapter` (agrupa salidas y valoración histórica).
- **Persistencia:** Tabla `inventory_clasificacion_producto` con migración `V49__init_wms_abc_schema.sql`.
- **Integración:** `AnalisisAbcIntegrationTest` con Testcontainers + MySQL.

---

## Submódulo WMS — Conteo Cíclico (Cycle Counting / Auditoría Logística)

### Sub-Bounded Context: `inventory/domain/model/conteo`
- **Aggregate Root:** [`ConteoCiclico`](file:///c:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/Api_Tienda/inventory/domain/model/conteo/ConteoCiclico.java)
- **Entidad Local:** [`DetalleConteo`](file:///c:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/Api_Tienda/inventory/domain/model/conteo/DetalleConteo.java) (encapsula `productoId`, `cantidadTeorica` y `cantidadFisica`).
- **Value Objects:**
  - `ConteoId`, `BodegaId`, `ProductoId`, `EmpresaId` (records Java 21 inmutables).
  - `CantidadFisica` (entero >= 0 con validación fail-fast).
  - `EstadoConteo` enum (`PLANIFICADO`, `EN_EJECUCION`, `COMPLETADO`, `CON_DISCREPANCIAS`).
- **Domain Event:** [`DiscrepanciaInventarioDetectadaEvent`](file:///c:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/Api_Tienda/inventory/domain/model/conteo/event/DiscrepanciaInventarioDetectadaEvent.java) (emite lista inmutable de `DiscrepanciaItem` si al finalizar hay descuadres).
- **Output Port:** [`ConteoCiclicoRepository`](file:///c:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/Api_Tienda/inventory/domain/model/conteo/port/ConteoCiclicoRepository.java) (interfaz pura de persistencia con `EmpresaId` obligatorio en todas sus firmas).
- **Application Layer:**
  - `RegistrarConteoFisicoCommand`, `FinalizarConteoCommand`, `ConteoCiclicoResponse`, `DetalleConteoResponse`.
  - Driving Ports: `RegistrarConteoFisicoUseCase`, `FinalizarConteoUseCase`.
  - Application Service: `EjecutarConteoCiclicoService` (`@Transactional`, extracción segura MT-01 de `empresa_id` vía `TenantProviderPort`, emisión reactiva de eventos).
- **Infrastructure Layer:**
  - REST Controller: `ConteoCiclicoController` (`PATCH /inventory/conteos/{id}/fisico`, `POST /inventory/conteos/{id}/finalizar`).
  - DTOs Web & Mappers: `RegistrarConteoFisicoWebRequest`, `ConteoCiclicoWebResponse`, `DetalleConteoWebResponse`, `ConteoWebMapper`.
  - JPA Persistence: `ConteoCiclicoJpaEntity`, `DetalleConteoJpaEntity` heredando de `AuditableJpaEntity`, `SpringDataConteoCiclicoRepository`.
  - Driven Adapter: `ConteoCiclicoJpaAdapter` con gestión de cascada y orfandad.
  - Flyway Migration: `V51__init_wms_cycle_counting_schema.sql` (tablas `inventory_conteo_ciclico` y `inventory_detalle_conteo` con partición multi-tenant MT-01).
- **Testing Suite:**
  - `ConteoCiclicoTest` (13 tests unitarios puros aprobados 13/13).
  - `ConteoCiclicoIntegrationTest` (Prueba de integración con Testcontainers MySQL 8.4, MockMvc y captura de eventos).
- **Estado:** 🟢 Capas de Dominio, Aplicación e Infraestructura Certificadas (`BUILD SUCCESS`).



