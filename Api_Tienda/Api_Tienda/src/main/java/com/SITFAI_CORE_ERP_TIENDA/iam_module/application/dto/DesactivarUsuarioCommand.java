package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto;

import java.util.UUID;

/**
 * Command DTO para la desactivación / inhabilitación de un Usuario.
 */
public record DesactivarUsuarioCommand(
        UUID id,
        UUID empresaId,
        String motivo
) {}
