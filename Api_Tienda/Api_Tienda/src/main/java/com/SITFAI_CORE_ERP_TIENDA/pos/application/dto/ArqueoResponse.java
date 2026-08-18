package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;

/**
 * Response DTO: Datos del Arqueo consolidado de Caja (CAJ-05).
 * <p>
 * Record inmutable de Java 25 puro.
 */
public record ArqueoResponse(
        BigDecimal montoInicial,
        BigDecimal totalVentas,
        BigDecimal totalDevoluciones,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal balanceEsperado,
        String moneda
) {}
