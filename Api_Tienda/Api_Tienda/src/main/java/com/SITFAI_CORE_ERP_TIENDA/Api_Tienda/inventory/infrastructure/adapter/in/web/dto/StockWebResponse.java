package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Web Response DTO: Representación HTTP de la consulta de stock.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-5).
 */
public record StockWebResponse(
        String bodegaId,
        String productoId,
        BigDecimal stock
) {}
