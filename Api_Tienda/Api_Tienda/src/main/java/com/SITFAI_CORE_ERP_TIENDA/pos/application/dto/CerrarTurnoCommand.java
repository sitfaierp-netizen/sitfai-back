package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CerrarTurnoCommand(
        UUID empresaId,
        UUID turnoId,
        BigDecimal montoDeclarado
) {
}
