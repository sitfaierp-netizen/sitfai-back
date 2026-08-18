package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto;

import java.util.UUID;

/**
 * Command DTO para modificar el rol de un Usuario existente.
 */
public record CambiarRolUsuarioCommand(
        UUID id,
        UUID empresaId,
        String nuevoRol
) {}
