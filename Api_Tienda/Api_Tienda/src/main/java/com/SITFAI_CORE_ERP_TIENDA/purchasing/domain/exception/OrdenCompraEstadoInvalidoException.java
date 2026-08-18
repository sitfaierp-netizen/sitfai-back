package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception;

/**
 * Excepción lanzada cuando una operación no es permitida en el estado actual de la Orden de Compra.
 */
public class OrdenCompraEstadoInvalidoException extends PurchasingDomainException {

    public OrdenCompraEstadoInvalidoException(String message) {
        super(message);
    }
}
