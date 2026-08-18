package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO Web: Representación de salida de una línea de orden.
 */
public record LineaOrdenWebResponse(
        UUID id,
        UUID productoId,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        BigDecimal subtotal,
        String moneda
) {
}
