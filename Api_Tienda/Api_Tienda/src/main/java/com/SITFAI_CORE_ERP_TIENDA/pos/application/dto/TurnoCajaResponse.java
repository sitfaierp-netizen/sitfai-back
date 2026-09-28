package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TurnoCajaResponse(
        UUID id,
        UUID empresaId,
        UUID cajaId,
        UUID sucursalId,
        UUID CajeroId,
        String estado,
        BigDecimal montoApertura,
        BigDecimal consolidadoActual,
        List<TransaccionCajaResponse> transacciones
) {
}
