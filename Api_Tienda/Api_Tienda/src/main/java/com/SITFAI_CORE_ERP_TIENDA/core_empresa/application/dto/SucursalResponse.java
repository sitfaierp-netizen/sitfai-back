package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO: Representación inmutable de una Sucursal para la capa de aplicación.
 */
public record SucursalResponse(
        UUID id,
        String codigo,
        String nombre,
        String estado,
        Instant creadoEn,
        Instant actualizadoEn
) {}
