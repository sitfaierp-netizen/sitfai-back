package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception;

/**
 * Excepción base para violaciones de reglas de negocio en el Bounded Context purchasing.
 */
public class PurchasingDomainException extends RuntimeException {

    public PurchasingDomainException(String message) {
        super(message);
    }

    public PurchasingDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
