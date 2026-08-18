package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Payload HTTP para reactivar un Usuario.
 */
public record ReactivarUsuarioRequest(
        UUID empresaId
) {}
