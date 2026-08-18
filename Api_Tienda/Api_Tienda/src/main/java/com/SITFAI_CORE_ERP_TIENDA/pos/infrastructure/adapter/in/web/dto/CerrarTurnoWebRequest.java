package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record CerrarTurnoWebRequest(
        BigDecimal montoDeclarado
) {
}
