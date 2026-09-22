package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo;

/**
 * Estados fiscales y de ciclo de vida del Agregado {@code Factura}.
 * <p>
 * Regla AUD-04: Los documentos financieros no se eliminan físicamente;
 * nacen como {@code EMITIDA} y solo pueden transicionar a {@code ANULADA}.
 */
public enum EstadoFactura {
    EMITIDA,
    ANULADA;

    public boolean esAnulable() {
        return this == EMITIDA;
    }
}
