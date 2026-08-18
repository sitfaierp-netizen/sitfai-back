package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Web Response DTO: Representación HTTP completa de un pedido.
 * <p>
 * Pertenece a la Capa de Infraestructura (REGLA-5).
 */
public record PedidoWebResponse(
        UUID id,
        UUID empresaId,
        UUID clienteId,
        String estado,
        BigDecimal total,
        String moneda,
        List<LineaPedidoWebResponse> lineas,
        Instant creadoEn,
        Instant actualizadoEn
) {}
