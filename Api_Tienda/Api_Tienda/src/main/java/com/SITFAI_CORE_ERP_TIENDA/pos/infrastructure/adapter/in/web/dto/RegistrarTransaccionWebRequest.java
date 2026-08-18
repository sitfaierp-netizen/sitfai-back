package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record RegistrarTransaccionWebRequest(
        String tipoTransaccion,
        BigDecimal monto,
        String referencia
) {
}
