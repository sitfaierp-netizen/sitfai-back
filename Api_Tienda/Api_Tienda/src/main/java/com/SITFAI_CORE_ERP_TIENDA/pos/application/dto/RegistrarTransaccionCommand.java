package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RegistrarTransaccionCommand(
        UUID empresaId,
        UUID turnoId,
        String TipoTransaccionCaja,
        BigDecimal monto,
        String referencia
) {
}
