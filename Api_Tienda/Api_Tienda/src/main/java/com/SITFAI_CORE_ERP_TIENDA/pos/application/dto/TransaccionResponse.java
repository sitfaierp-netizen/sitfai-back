package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO: Datos de salida inmutables de una Transacción individual de Caja.
 * <p>
 * Record inmutable de Java 25 puro.
 */
public record TransaccionResponse(
        UUID id,
        String tipoTransaccion,
        BigDecimal monto,
        String moneda,
        String concepto,
        Instant registradoEn
) {}
