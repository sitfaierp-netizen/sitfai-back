package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AbrirTurnoWebRequest(
        UUID cajaId,
        UUID sucursalId,
        UUID usuarioId,
        BigDecimal montoApertura
) {
}
