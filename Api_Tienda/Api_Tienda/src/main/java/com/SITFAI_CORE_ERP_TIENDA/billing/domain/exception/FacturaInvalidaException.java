package com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception;

/**
 * Excepción lanzada cuando una operación o estado viola las invariantes del Agregado {@code Factura}.
 */
public class FacturaInvalidaException extends DomainException {

    public FacturaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
