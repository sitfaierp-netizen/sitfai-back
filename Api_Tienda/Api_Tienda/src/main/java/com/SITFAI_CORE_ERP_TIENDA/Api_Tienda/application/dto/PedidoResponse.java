package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO: Representación inmutable del estado del Pedido en la capa de aplicación.
 * <p>
 * Record puro de Java 25 (REGLA-1, REGLA-2).
 */
public record PedidoResponse(
        UUID id,
        UUID empresaId,
        UUID clienteId,
        String estado,
        BigDecimal total,
        String moneda,
        List<LineaPedidoResponse> lineas,
        Instant creadoEn,
        Instant actualizadoEn
) {}
