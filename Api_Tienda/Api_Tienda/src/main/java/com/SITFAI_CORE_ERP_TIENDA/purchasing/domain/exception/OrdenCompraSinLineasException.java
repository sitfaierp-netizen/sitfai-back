package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception;

/**
 * Excepción lanzada al intentar aprobar o procesar una orden de compra sin líneas de detalle.
 */
public class OrdenCompraSinLineasException extends PurchasingDomainException {

    public OrdenCompraSinLineasException(String message) {
        super(message);
    }
}
