package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Web Response DTO: Representación HTTP de una línea de pedido.
 * <p>
 * Pertenece a la Capa de Infraestructura (REGLA-5).
 */
public record LineaPedidoWebResponse(
        UUID id,
        UUID productoId,
        int cantidad,
        BigDecimal precioUnitario,
        String moneda,
        BigDecimal subtotal
) {}
