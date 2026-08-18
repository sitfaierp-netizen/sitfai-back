package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception;

/**
 * Clase base para todas las Excepciones de Dominio del Bounded Context de Inventario.
 * <p>
 * Hereda de {@code RuntimeException} (unchecked) — no de ningún framework.
 * Los adaptadores de Infraestructura (REST, Mensajería) son responsables de
 * transformar estas excepciones al protocolo adecuado (ej. RFC 7807 Problem Details).
 * <p>
 * Reglas validadas: REGLA-1 (cero frameworks en dominio), MCP-01 (aislamiento total),
 * REGLA-6 (Protocolo de Cero Alucinaciones).
 */
public abstract class DomainException extends RuntimeException {

    /**
     * Código de error semántico del dominio (ej. "BOD-05", "BOD-04").
     * Permite a los adaptadores mapear excepciones sin inspeccionar el mensaje.
     */
    private final String codigoError;

    protected DomainException(String codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    protected DomainException(String codigoError, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = codigoError;
    }

    /**
     * Retorna el código de error de negocio asociado a esta excepción.
     * Útil para los adaptadores REST que necesitan construir Problem Details (RFC 7807).
     */
    public String getCodigoError() {
        return codigoError;
    }
}
