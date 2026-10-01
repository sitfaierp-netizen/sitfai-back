package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security;

/**
 * Raised when the requested tenant cannot be resolved from trusted authentication
 * data. The HTTP layer deliberately maps it to 404 to avoid disclosing whether a
 * resource exists in another tenant.
 */
public class TenantScopeViolationException extends RuntimeException {

    public TenantScopeViolationException() {
        super("Recurso no encontrado.");
    }

    public TenantScopeViolationException(Throwable cause) {
        super("Recurso no encontrado.", cause);
    }
}
