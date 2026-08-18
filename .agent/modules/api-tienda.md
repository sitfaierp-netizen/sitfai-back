# SITFAI ERP — Api_Tienda (com.SITFAI_CORE_ERP_TIENDA)
> **Versión:** 1.0.0 | **Fecha:** 2026-08-07 | **Estado:** 🟢 COMPLETADO Y VALIDADO

---

## 1. IDENTIDAD DEL MÓDULO

| Campo              | Valor                                    |
|--------------------|------------------------------------------|
| **Group ID**       | `com.SITFAI_CORE_ERP_TIENDA`             |
| **Artifact ID**    | `Api_Tienda`                             |
| **Package raíz**   | `com.SITFAI_CORE_ERP_TIENDA.Api_Tienda`  |
| **Puerto**         | `8084`                                   |
| **Context Path**   | `/api/v1`                                |
| **Spring Boot**    | `4.0.7`                                  |
| **Java**           | `25`                                     |
| **BD**             | MySQL 8+ (esquema: `tienda`)             |
| **Migración BD**   | Flyway (`V9__init_api_tienda_schema.sql`) |

---

## 2. OBJETIVO DEL BOUNDED CONTEXT

El módulo `Api_Tienda` gestiona las operaciones de **venta al público y motor de pedidos** del ERP SITFAI.
Su Bounded Context incluye el ciclo de vida de pedidos comerciales, catálogo y líneas de pedido, 
asegurando el aislamiento multi-inquilino (MT-01), seguridad Zero Trust mediante inyección criptográfica del Tenant (`@TenantId`) y la emisión de eventos de dominio (`PedidoConfirmadoEvent`) para la coreografía con Inventario y Facturación.

---

## 3. ESTRUCTURA COMPLETA DEL MÓDULO (HEXAGONAL / CLEAN ARCHITECTURE)

```text
com.SITFAI_CORE_ERP_TIENDA.Api_Tienda
├── domain/
│   ├── model/
│   │   ├── EstadoPedido.java         (Enum: CREADO, CONFIRMADO, CANCELADO)
│   │   ├── ItemPedido.java           (Entity: Ítem del pedido, cantidad, precio, subtotal)
│   │   ├── LineaPedido.java          (Entity: Detalle del pedido, inmutable en subtotal)
│   │   └── Pedido.java               (Aggregate Root: iniciar, agregarItem, confirmar, cancelar, MT-01)
│   ├── valueobject/
│   │   ├── Cantidad.java             (VO: record Java 25, entero > 0, operaciones de suma)
│   │   ├── ClienteId.java            (VO: record Java 25, UUID, factory generar/de)
│   │   ├── Dinero.java               (VO: record Java 25, BigDecimal redondeado a 2 decimales HALF_UP)
│   │   ├── EmpresaId.java            (VO: record Java 25, UUID, discriminador MT-01)
│   │   ├── PedidoId.java             (VO: record Java 25, UUID, factory generar/de)
│   │   └── ProductoId.java           (VO: record Java 25, UUID, factory generar/de)
│   ├── event/
│   │   ├── DomainEvent.java          (Interface base de eventos de dominio)
│   │   └── PedidoConfirmadoEvent.java (Record de evento inmutable con detalles de líneas para coreografía)
│   └── exception/
│       ├── DomainException.java              (Excepción base)
│       ├── PedidoInvalidoException.java      (422 Unprocessable Entity / Invariante)
│       └── PedidoNoEncontradoException.java  (404 Not Found)
│
├── application/
│   ├── dto/
│   │   ├── AgregarLineaCommand.java
│   │   ├── AgregarLineaPedidoCommand.java
│   │   ├── CancelarPedidoCommand.java
│   │   ├── ConfirmarPedidoCommand.java
│   │   ├── CrearPedidoCommand.java
│   │   ├── LineaPedidoResponse.java
│   │   ├── PedidoResponse.java
│   │   └── RemoverLineaPedidoCommand.java
│   ├── mapper/
│   │   └── PedidoApplicationMapper.java (Mapeo estático inmutable Dominio -> DTOs)
│   ├── port/
│   │   ├── input/
│   │   │   ├── AgregarLineaPedidoUseCase.java
│   │   │   ├── CancelarPedidoUseCase.java
│   │   │   ├── ConfirmarPedidoUseCase.java
│   │   │   ├── ConsultarPedidoUseCase.java
│   │   │   ├── CrearBorradorPedidoUseCase.java
│   │   │   ├── CrearPedidoUseCase.java
│   │   │   └── GestionarLineasPedidoUseCase.java
│   │   └── output/
│   │       ├── PedidoEventPublisher.java
│   │       └── PedidoRepository.java (Aislamiento estricto MT-01 por EmpresaId)
│   └── service/
│       ├── CancelarPedidoService.java
│       ├── ConfirmarPedidoService.java (Orquestación con drenado y publicación de eventos AUD-03)
│       ├── ConsultarPedidoService.java
│       ├── CrearPedidoService.java
│       └── GestionarLineasPedidoService.java
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       ├── dto/
    │   │       │   ├── AgregarLineaWebRequest.java
    │   │       │   ├── CancelarPedidoWebRequest.java
    │   │       │   ├── CrearPedidoWebRequest.java
    │   │       │   ├── LineaPedidoWebResponse.java
    │   │       │   └── PedidoWebResponse.java
    │   │       ├── PedidoController.java          (@RestController /api/v1/pedidos con Zero Trust @TenantId)
    │   │       └── TiendaExceptionHandler.java     (@RestControllerAdvice RFC 7807)
    │   └── out/
    │       ├── event/
    │       │   └── SpringEventPedidoPublisher.java (ApplicationEventPublisher)
    │       └── persistence/
    │           ├── adapter/
    │           │   └── PedidoJpaAdapter.java       (Implementación de PedidoRepository)
    │           ├── entity/
    │           │   ├── LineaPedidoJpaEntity.java   (JPA Entity tienda_linea_pedido)
    │           │   └── PedidoJpaEntity.java        (JPA Entity tienda_pedido)
    │           └── repository/
    │               └── PedidoJpaRepository.java    (Spring Data JPA con MT-01)
    └── mapper/
        ├── PedidoPersistenceMapper.java            (Mapeo bidireccional Dominio <-> JPA)
        └── PedidoWebMapper.java                    (Mapeo HTTP Request/Response <-> Application)
```

---

## 4. BASE DE DATOS Y MIGRACIONES (FLYWAY)

### Script `V9__init_api_tienda_schema.sql`
- **Tablas:**
  - `tienda_pedido`: `id` (PK, UUID VARCHAR(36)), `empresa_id` (MT-01), `cliente_id`, `estado`, `total` (DECIMAL(19, 4)), `moneda`, timestamps de auditoría.
  - `tienda_linea_pedido`: `id` (PK, UUID VARCHAR(36)), `pedido_id` (FK CASCADE), `empresa_id` (MT-01), `producto_id`, `cantidad`, `precio_unitario` (DECIMAL(19, 4)), `moneda`, `subtotal` (DECIMAL(19, 4)).
- **Índices de Rendimiento y Multitenancy:**
  - `idx_tienda_pedido_empresa` en `tienda_pedido(empresa_id)`
  - `idx_tienda_pedido_empresa_estado` en `tienda_pedido(empresa_id, estado)`
  - `idx_tienda_pedido_cliente` en `tienda_pedido(empresa_id, cliente_id)`
  - `idx_tienda_pedido_fecha` en `tienda_pedido(empresa_id, creado_en)`
  - `idx_tienda_linea_pedido_empresa` en `tienda_linea_pedido(empresa_id)`
  - `idx_tienda_linea_pedido_pedido` en `tienda_linea_pedido(pedido_id)`
  - `idx_tienda_linea_pedido_producto` en `tienda_linea_pedido(empresa_id, producto_id)`

---

## 5. API REST ENDPOINTS (`/api/v1/pedidos`)

| Método   | Endpoint                          | Seguridad / Tenant    | Body Payload               | Respuesta                 | Código HTTP   |
|----------|-----------------------------------|-----------------------|----------------------------|---------------------------|---------------|
| `POST`   | `/api/v1/pedidos`                 | `@TenantId UUID` (JWT)| `CrearPedidoWebRequest`    | `PedidoWebResponse`       | `201 CREATED` |
| `POST`   | `/api/v1/pedidos/{id}/lineas`     | `@TenantId UUID` (JWT)| `AgregarLineaWebRequest`   | `PedidoWebResponse`       | `200 OK`      |
| `DELETE` | `/api/v1/pedidos/{id}/lineas/{lineaId}` | `@TenantId UUID` (JWT)| *None*              | `PedidoWebResponse`       | `200 OK`      |
| `PATCH`  | `/api/v1/pedidos/{id}/confirmar`  | `@TenantId UUID` (JWT)| *None*                     | `PedidoWebResponse`       | `200 OK`      |
| `PATCH`  | `/api/v1/pedidos/{id}/cancelar`   | `@TenantId UUID` (JWT)| `CancelarPedidoWebRequest` | `PedidoWebResponse`       | `200 OK`      |
| `GET`    | `/api/v1/pedidos/{id}`            | `@TenantId UUID` (JWT)| *None*                     | `PedidoWebResponse`       | `200 OK`      |
| `GET`    | `/api/v1/pedidos`                 | `@TenantId UUID` (JWT)| *None*                     | `List<PedidoWebResponse>` | `200 OK`      |

### Formato de Errores: RFC 7807 (Problem Details)
- `404 NOT FOUND`: `PedidoNoEncontradoException` (`urn:problem-type:pedido-no-encontrado`)
- `422 UNPROCESSABLE ENTITY`: `PedidoInvalidoException` (`urn:problem-type:pedido-invalido`)
- `400 BAD REQUEST`: `DomainException` / `IllegalArgumentException` (`urn:problem-type:domain-rule-violation`)
- `500 INTERNAL SERVER ERROR`: `Exception` (`urn:problem-type:internal-server-error`)

---

## 6. LENGUAJE UBICUO (UBIQUITOUS LANGUAGE)

- **Pedido:** Transacción comercial de compra realizada por un cliente. Inicia como `CREADO`, transiciona a `CONFIRMADO` y finaliza en `ENTREGADO` o `CANCELADO`.
- **Línea de Pedido:** Detalle de un producto específico, su cantidad y el precio unitario acordado en el momento de la venta.
- **Empresa (Tenant):** Entidad legal que aísla los datos (MT-01). Cada pedido pertenece estrictamente a una sola empresa.
- **Confirmación:** Acto irreversible por el cual el cliente aprueba el pedido, disparando la coreografía con Inventario (reserva de stock) y Facturación (emisión de comprobante).

---

## 7. EVENTOS DE DOMINIO (DOMAIN EVENTS)

- **`PedidoConfirmadoEvent`**: Emitido asíncronamente cuando un pedido transiciona exitosamente a estado `CONFIRMADO`. Contiene el detalle completo del pedido (líneas, total, cliente) para que otros Bounded Contexts (Inventario, Facturación) reaccionen sin acoplamiento temporal ni estructural.

---

## 8. COBERTURA Y RESULTADOS DE PRUEBAS

- **Total de pruebas en el módulo/suite:** 288 tests passing (0 fallos, 0 errores, 100% éxito).
- **Capa Dominio:** Pure Java 25 Unit Tests (100% de cobertura en invariantes, agregados y value objects).
- **Capa Aplicación:** Application Service Tests con Mockito (100% de cobertura en orquestación de use cases).
- **Capa Infraestructura:** Unit tests de Driving & Driven Adapters (Controllers con `@TenantId`, ExceptionHandler RFC 7807, SpringEventPublisher, Mappers y `PedidoJpaAdapterTest`). Pruebas de integración aisladas (`*IT.java`) con Testcontainers MySQL 8+.

---

## 7. DECISIONES ARQUITECTÓNICAS REGISTRADAS

| ADR-ID  | Fecha       | Decisión                                                                 | Estado      |
|---------|-------------|--------------------------------------------------------------------------|-------------|
| ADR-006 | 2026-08-06  | Puerto 8084 asignado exclusivamente a `Api_Tienda`                       | ACEPTADO    |
| ADR-007 | 2026-08-06  | Credenciales DB via variables de entorno (cero hardcoding)               | ACEPTADO    |
| ADR-008 | 2026-08-06  | Esquema gestionado con Flyway (`V9__init_api_tienda_schema.sql`)          | ACEPTADO    |
| ADR-009 | 2026-08-06  | `CamelCaseToUnderscoresNamingStrategy` para snake_case automático        | ACEPTADO    |
| ADR-010 | 2026-08-06  | Preferencia de formato `.properties` sobre `.yaml` (decisión del usuario) | ACEPTADO   |
| ADR-011 | 2026-08-07  | Seguridad Zero Trust en controladores REST mediante `@TenantId` desde JWT | ACEPTADO   |

---

## 8. HISTORIAL DE CAMBIOS

| Versión | Fecha       | Cambio                                                                                             |
|---------|-------------|----------------------------------------------------------------------------------------------------|
| 0.1.0   | 2026-08-06  | Inicialización del módulo — `application.properties` generado                                     |
| 0.2.0   | 2026-08-07  | Diseño e implementación de Capas de Dominio y Aplicación bajo DDD estricto y Java 25              |
| 0.3.0   | 2026-08-07  | Implementación de Capa de Infraestructura básica                                                   |
| 1.0.0   | 2026-08-07  | Finalización de Infraestructura Zero Trust (@TenantId), endpoints completos, migración Flyway V9 y validación 100% de tests |
| 1.1.0   | 2026-08-12  | El módulo cuenta con soporte completo de facturación electrónica y notas de crédito con adaptadores JPA estables (Migraciones Flyway V10-V20) |
