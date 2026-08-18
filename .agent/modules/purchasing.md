# Módulo: purchasing — Compras y Reabastecimiento (Replenishment)
> **Estado:** 🟢 COMPLETADO Y VALIDADO | **Fecha:** 2026-08-11 | **Revisión:** 1.2.0

---

## Bounded Context

**Nombre:** `purchasing`
**Paquete raíz:** `com.SITFAI_CORE_ERP_TIENDA.purchasing`
**Responsabilidad:** Gestión de reabastecimiento, generación de Órdenes de Compra a proveedores y su ciclo de vida (desde borrador hasta recepción). Trabaja en estricta coordinación con `inventory` para asegurar la entrada física.

---

## Lenguaje Ubicuo (Ubiquitous Language)

- **Orden de Compra (`OrdenCompra`):** Documento comercial que autoriza la adquisición de bienes a un proveedor.
- **Línea de Orden (`LineaOrdenCompra`):** Detalle de cantidad y costo pactado por cada producto.
- **Costo Exacto (`Dinero`):** Value Object matemático que garantiza redondeo a 4 decimales (`HALF_UP`) para cumplir con normativas de costeo promedio.
- **EstadoOrden:**
  - `BORRADOR`: Orden en preparación, permite añadir líneas.
  - `EMITIDA`: Orden finalizada y enviada al proveedor. Es inmutable.
  - `RECIBIDA`: Orden cuya mercadería ha llegado, dispara el evento `OrdenCompraRecibidaEvent` consumido por inventario.
  - `CANCELADA`: Orden abortada.

---

## Reglas de Negocio Implementadas

| ID     | Regla                                                              | Implementación                                    |
|--------|--------------------------------------------------------------------|---------------------------------------------------|
| PUR-01 | Precisión contable estricta en compras (Cero Floats, 4 decimales)  | Value Object `Dinero` con `setScale(4, HALF_UP)`  |
| PUR-02 | Inmutabilidad Comercial                                            | Invariante: Solo en `BORRADOR` se agregan líneas  |
| PUR-03 | Prevención de Emisión Vacía                                        | Invariante en `OrdenCompra.emitir()`              |
| PUR-04 | Acoplamiento mediante Eventos hacia Inventario                     | Emisión de `OrdenCompraRecibidaEvent` al recibir  |
| MT-01  | `empresa_id` obligatorio en todas las transacciones                | Value Object `EmpresaId` inyectado en Factory     |

---

## Estado de Capas

| Capa | Estado |
|---|---|
| **Domain** | ✅ COMPLETADO |
| **Application** | ✅ COMPLETADO |
| **Infrastructure** | ✅ COMPLETADO |
| **Testing Suite** | ✅ Domain & App Tests pasando |
