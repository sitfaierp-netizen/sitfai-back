package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception;

/**
 * Excepción base del Dominio catalog. Sin dependencias a frameworks.
 * Toda violación de invariante del dominio lanza esta excepción o una subclase.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
