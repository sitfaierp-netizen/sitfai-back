package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security;

import java.util.Objects;
import java.util.UUID;

/**
 * Detalles de autenticación específicos para Multitenancy (MT-01, MT-06).
 * Almacena el identificador del tenant extraído criptográficamente del token JWT.
 */
public record TenantAuthenticationDetails(String empresaId) {

    public TenantAuthenticationDetails {
        Objects.requireNonNull(empresaId, "empresaId no puede ser null");
    }

    public UUID empresaUuid() {
        return UUID.fromString(empresaId);
    }
}
