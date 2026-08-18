package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Web Response DTO para una Sucursal.
 */
public record SucursalWebResponse(
        UUID id,
        String codigo,
        String nombre,
        String estado,
        Instant creadoEn,
        Instant actualizadoEn
) {}
