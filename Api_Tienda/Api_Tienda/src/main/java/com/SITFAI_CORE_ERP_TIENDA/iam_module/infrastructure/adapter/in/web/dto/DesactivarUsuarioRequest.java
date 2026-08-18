package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Payload HTTP para desactivar un Usuario.
 */
public record DesactivarUsuarioRequest(
        UUID empresaId,
        String motivo
) {}
