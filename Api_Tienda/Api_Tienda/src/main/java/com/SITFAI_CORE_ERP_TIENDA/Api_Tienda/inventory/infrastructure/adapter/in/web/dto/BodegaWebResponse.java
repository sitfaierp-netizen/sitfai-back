package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.time.Instant;

/**
 * Web Response DTO: Representación HTTP de una Bodega.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-5).
 */
public record BodegaWebResponse(
        String id,
        String empresaId,
        String sucursalId,
        String codigo,
        String nombre,
        boolean activa,
        Instant creadoEn
) {}
