package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto;

import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        String ruc,
        String nombre,
        String estado,
        java.util.List<SucursalResponse> sucursales,
        java.time.Instant creadoEn,
        java.time.Instant actualizadoEn
) {
}
