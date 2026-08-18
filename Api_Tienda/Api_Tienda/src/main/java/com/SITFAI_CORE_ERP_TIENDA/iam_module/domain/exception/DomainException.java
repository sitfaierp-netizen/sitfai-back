package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception;

/**
 * Excepción base para todas las violaciones de reglas de negocio en el Dominio IAM.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
