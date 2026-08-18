package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.exception;

/**
 * Excepción de negocio lanzada cuando se detecta un SKU duplicado en la misma empresa.
 * Traducida desde DataIntegrityViolationException (UNIQUE empresa_id+sku).
 * El ExceptionHandler la mapea a HTTP 409 Conflict (RFC 7807).
 */
public class SkuDuplicadoException extends RuntimeException {
    public SkuDuplicadoException(String sku, String empresaId) {
        super("El SKU '" + sku + "' ya existe en el catálogo de la empresa: " + empresaId);
    }
}
