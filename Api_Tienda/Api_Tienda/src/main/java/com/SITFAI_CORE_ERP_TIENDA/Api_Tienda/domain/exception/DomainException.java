package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception;

/**
 * Excepción base abstracta para todas las violaciones de reglas de negocio en el Dominio.
 * <p>
 * Cero dependencias de Spring / Jakarta (REGLA-1).
 */
public abstract class DomainException extends RuntimeException {

    private final String codigoError;

    protected DomainException(String codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
