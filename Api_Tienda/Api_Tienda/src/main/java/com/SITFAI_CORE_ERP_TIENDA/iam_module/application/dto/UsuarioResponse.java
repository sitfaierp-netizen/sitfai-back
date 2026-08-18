package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO representando la información consolidada de un Usuario.
 */
public record UsuarioResponse(
        UUID id,
        UUID empresaId,
        String username,
        String email,
        String rol,
        String estado,
        Instant creadoEn,
        Instant actualizadoEn
) {}
