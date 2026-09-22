# Módulo: purchasing — Compras y Abastecimiento (Replenishment)
> **Estado:** 🟢 DOMINIO CERTIFICADO (Java 21 / Clean Architecture) | **Fecha:** 2026-09-22 | **Revisión:** 2.0.0

---

## Bounded Context

**Nombre:** `purchasing`  
**Paquete raíz:** `com.SITFAI_CORE_ERP_TIENDA.purchasing`  
**Puerto Asignado:** `8087` (Mapa de Puertos SITFAI ERP)  
**Responsabilidad:** Gestión de abastecimiento, negociación con proveedores y ciclo de vida de Órdenes de Compra (desde borrador hasta recepción física total o parcial). Actúa como documento fuente canónico (BOD-04) para la trazabilidad de stock en tránsito hacia el módulo de almacenes (`inventory`).

---

## Lenguaje Ubicuo (Ubiquitous Language)

- **Orden de Compra (`OrdenCompra`):** Aggregate Root que formaliza el requerimiento comercial y contractual hacia un proveedor.
- **Línea de Orden (`LineaOrdenCompra`):** Entidad interna protegida que detalla producto, cantidad solicitada y costo unitario esperado con cálculo de subtotal.
- **Estado de Orden de Compra (`EstadoOrdenCompra`):**
  - `BORRADOR`: Orden en preparación inicial; única fase donde se pueden agregar líneas.
  - `EMITIDA`: Orden formalmente notificada al proveedor adjudicado; stock declarado en tránsito (emite `OrdenCompraEmitidaEvent`).
  - `RECEPCION_PARCIAL`: Recepción inicial de mercancía en bodega con saldo pendiente.
  - `COMPLETADA`: Recepción total y cierre definitivo de la orden.
  - `CANCELADA`: Anulación de la orden antes de su recepción completa.

---

## Reglas de Negocio e Invariantes (Dominio)

| ID | Regla | Implementación en Dominio |
|---|---|---|
| **MT-01** | Aislamiento multi-tenant estricto por `empresa_id`. | Value Object `EmpresaId` encapsulado en `OrdenCompra`, `LineaOrdenCompra` y en todas las firmas del puerto `OrdenCompraRepository`. |
| **BOD-04** | Documento fuente obligatorio para movimientos de bodega. | `OrdenCompra` y su evento `OrdenCompraEmitidaEvent` actúan como documento fuente trazable de stock en tránsito. |
| **PUR-01** | Máquina de estados estricta y unidireccional. | `OrdenCompra.emitirAlProveedor()`, `marcarRecepcionParcial()`, `marcarCompletada()`, `cancelar()`. |
| **PUR-02** | Prevención de Emisión Vacía (Fail-fast). | Invariante inquebrantable: `emitirAlProveedor()` exige al menos una línea de detalle registrada. |
| **PUR-03** | Inmutabilidad Comercial post-emisión. | No se pueden agregar líneas a una orden emitida, completada o cancelada. |

---

## Diseño del Modelo (Clean Architecture - Dominio Puro)

- **Value Objects:** [`OrdenCompraId`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/vo/OrdenCompraId.java), [`ProveedorId`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/vo/ProveedorId.java), [`EmpresaId`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/vo/EmpresaId.java), [`ProductoId`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/vo/ProductoId.java), [`EstadoOrdenCompra`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/vo/EstadoOrdenCompra.java).
- **Entidades Protegidas:** [`LineaOrdenCompra`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/LineaOrdenCompra.java).
- **Aggregate Root:** [`OrdenCompra`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/OrdenCompra.java).
- **Eventos de Dominio:** [`OrdenCompraEmitidaEvent`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/event/OrdenCompraEmitidaEvent.java).
- **Puertos de Salida:** [`OrdenCompraRepository`](file:///C:/ERP_CORE/Api_Tienda/Api_Tienda/src/main/java/com/SITFAI_CORE_ERP_TIENDA/purchasing/domain/model/ordencompra/port/output/OrdenCompraRepository.java).
