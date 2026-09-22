package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo;

/**
 * Estados lógicos del ciclo de vida de una Orden de Compra en el módulo de Abastecimiento (BOD-04).
 * <ul>
 *   <li><b>BORRADOR:</b> Creación y edición inicial de las líneas de compra.</li>
 *   <li><b>EMITIDA:</b> Orden notificada/enviada al proveedor; stock en tránsito hacia la bodega.</li>
 *   <li><b>RECEPCION_PARCIAL:</b> Recepción preliminar de parte de los ítems en almacén.</li>
 *   <li><b>COMPLETADA:</b> Recepción total y cierre de la orden de compra.</li>
 *   <li><b>CANCELADA:</b> Anulación formal de la orden antes de su recepción definitiva.</li>
 * </ul>
 */
public enum EstadoOrdenCompra {
    BORRADOR,
    EMITIDA,
    RECEPCION_PARCIAL,
    COMPLETADA,
    CANCELADA;

    public boolean puedeModificarse() {
        return this == BORRADOR;
    }

    public boolean puedeEmitirse() {
        return this == BORRADOR;
    }

    public boolean puedeCancelarse() {
        return this == BORRADOR || this == EMITIDA;
    }
}
