package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CrearPedidoRequest(
        UUID clienteId,
        List<LineaRequest> lineas
) {
    public record LineaRequest(
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario
    ) {}
}
