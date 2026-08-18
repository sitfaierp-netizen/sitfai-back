package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO: Representación inmutable de una línea de detalle de pedido en la capa de aplicación.
 */
public record LineaPedidoResponse(
        UUID id,
        UUID productoId,
        int cantidad,
        BigDecimal precioUnitario,
        String moneda,
        BigDecimal subtotal
) {}
