package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO: Representación completa de un Turno de Caja (Sesión de POS) y sus movimientos asociados.
 * <p>
 * Record inmutable de Java 21 puro.
 */
public record TurnoResponse(
        UUID id,
        UUID cajaId,
        UUID empresaId,
        String estado,
        BigDecimal montoInicial,
        String moneda,
        List<TransaccionResponse> transacciones,
        ArqueoResponse arqueo,
        Instant abiertoEn,
        Instant cerradoEn,
        Instant creadoEn,
        Instant actualizadoEn
) {}
