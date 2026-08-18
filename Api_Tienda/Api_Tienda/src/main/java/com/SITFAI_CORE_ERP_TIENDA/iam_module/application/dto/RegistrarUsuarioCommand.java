package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto;

import java.util.UUID;

/**
 * Command DTO para el registro de un nuevo Usuario en un Tenant.
 * Java 21 puro sin anotaciones de frameworks web ni validación externa.
 */
public record RegistrarUsuarioCommand(
        UUID id,
        UUID empresaId,
        String username,
        String email,
        String rol
) {
    public RegistrarUsuarioCommand(UUID empresaId, String username, String email, String rol) {
        this(null, empresaId, username, email, rol);
    }
}
