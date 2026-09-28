package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record RegistrarTransaccionCajaCommand(
        UUID turnoId,
        String TipoTransaccionCaja,
        BigDecimal monto,
        String referencia,
        List<LineaTransaccion> lineas
) {
    public record LineaTransaccion(
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario
    ) {}
}
