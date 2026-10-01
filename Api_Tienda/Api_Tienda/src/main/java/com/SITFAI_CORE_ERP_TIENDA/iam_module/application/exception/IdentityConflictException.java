package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception;

/**
 * Conflicto entre la identidad local y una identidad externa preexistente.
 */
public class IdentityConflictException extends RuntimeException {

    public IdentityConflictException(String message) {
        super(message);
    }
}
