package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RegistrarTransaccionWebRequest(
        String tipoTransaccion,
        BigDecimal monto,
        String referencia,
        List<LineaTransaccionWebRequest> lineas
) {
    public record LineaTransaccionWebRequest(
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario
    ) {}
}
