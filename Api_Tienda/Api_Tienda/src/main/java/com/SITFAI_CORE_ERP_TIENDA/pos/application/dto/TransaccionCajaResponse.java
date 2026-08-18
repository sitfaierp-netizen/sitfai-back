package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransaccionCajaResponse(
        UUID id,
        String tipo,
        BigDecimal monto,
        String referencia,
        Instant fecha
) {
}
