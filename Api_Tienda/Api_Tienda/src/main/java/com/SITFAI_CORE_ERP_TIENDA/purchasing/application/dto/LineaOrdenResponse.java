package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de Aplicación: Representación inmutable de una línea de orden de compra.
 * Record puro de Java 25.
 */
public record LineaOrdenResponse(
        UUID id,
        UUID productoId,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        BigDecimal subtotal,
        String moneda
) {
}
