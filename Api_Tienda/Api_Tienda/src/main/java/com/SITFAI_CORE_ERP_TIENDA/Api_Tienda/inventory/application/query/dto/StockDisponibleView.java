package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO plano de lectura (CQRS) para representar el stock actual disponible de un producto en una bodega.
 */
public record StockDisponibleView(
    String empresaId,
    String bodegaId,
    String productoId,
    BigDecimal cantidadTotal,
    Instant ultimaActualizacion
) {}
