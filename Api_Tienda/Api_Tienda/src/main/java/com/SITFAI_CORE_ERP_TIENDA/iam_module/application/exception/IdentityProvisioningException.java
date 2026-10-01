package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception;

/**
 * Error recuperable al comunicarse con el proveedor externo de identidad.
 */
public class IdentityProvisioningException extends RuntimeException {

    public IdentityProvisioningException(String message) {
        super(message);
    }

    public IdentityProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
