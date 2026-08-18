package com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception;

/**
 * Excepción base para el Dominio de Billing (Facturación).
 * <p>
 * Java 25 puro, sin frameworks externos (REGLA-1, MCP-01).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensaje) {
        super(mensaje);
    }

    protected DomainException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
