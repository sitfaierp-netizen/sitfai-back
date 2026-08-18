package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Payload HTTP para el registro de un nuevo Usuario.
 */
public record RegistrarUsuarioRequest(
        UUID id,
        UUID empresaId,
        String username,
        String email,
        String rol
) {}
