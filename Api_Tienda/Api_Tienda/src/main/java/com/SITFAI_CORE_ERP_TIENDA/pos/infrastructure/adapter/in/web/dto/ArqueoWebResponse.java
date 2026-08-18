package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Web Response DTO: Representación JSON del arqueo de caja.
 */
public record ArqueoWebResponse(
        BigDecimal montoInicial,
        BigDecimal totalVentas,
        BigDecimal totalDevoluciones,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal balanceEsperado,
        String moneda
) {}
