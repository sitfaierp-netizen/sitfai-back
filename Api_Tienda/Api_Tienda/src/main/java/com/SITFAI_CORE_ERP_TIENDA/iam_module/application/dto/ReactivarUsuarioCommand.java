package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto;

import java.util.UUID;

/**
 * Command DTO para reactivar un Usuario inactivo.
 */
public record ReactivarUsuarioCommand(
        UUID id,
        UUID empresaId
) {}
