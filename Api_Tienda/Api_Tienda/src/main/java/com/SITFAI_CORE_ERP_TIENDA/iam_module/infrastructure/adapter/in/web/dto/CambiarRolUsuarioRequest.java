package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Payload HTTP para modificar el rol de un Usuario.
 */
public record CambiarRolUsuarioRequest(
        UUID empresaId,
        String nuevoRol
) {}
