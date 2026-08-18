package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Web Response DTO: Representación JSON de una transacción de caja.
 */
public record TransaccionWebResponse(
        UUID id,
        String tipoTransaccion,
        BigDecimal monto,
        String moneda,
        String concepto,
        Instant registradoEn
) {}
