package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception;

/**
 * Excepción base para todas las violaciones de invariantes y reglas de negocio
 * del Bounded Context core-empresa (Regla 1, Regla 3).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
