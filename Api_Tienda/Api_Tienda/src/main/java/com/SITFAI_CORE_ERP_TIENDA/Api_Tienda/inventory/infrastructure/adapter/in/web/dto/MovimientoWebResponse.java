package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Web Response DTO: Representación HTTP del registro de un movimiento.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-5).
 */
public record MovimientoWebResponse(
        String movimientoId,
        String bodegaId,
        String productoId,
        String tipo,
        BigDecimal cantidad,
        BigDecimal stockResultante,
        String documentoFuente,
        Instant fechaRegistro
) {}
