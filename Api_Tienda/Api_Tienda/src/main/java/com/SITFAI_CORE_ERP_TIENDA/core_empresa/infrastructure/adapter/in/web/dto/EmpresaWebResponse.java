package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Web Response DTO para una Empresa.
 */
public record EmpresaWebResponse(
        UUID id,
        String ruc,
        String nombre,
        String estado,
        List<SucursalWebResponse> sucursales,
        Instant creadoEn,
        Instant actualizadoEn
) {}
