package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception;

/**
 * Excepción lanzada cuando se violan invariantes del modelo de Usuario o sus Value Objects.
 */
public class UsuarioInvalidoException extends DomainException {

    public UsuarioInvalidoException(String message) {
        super(message);
    }
}
