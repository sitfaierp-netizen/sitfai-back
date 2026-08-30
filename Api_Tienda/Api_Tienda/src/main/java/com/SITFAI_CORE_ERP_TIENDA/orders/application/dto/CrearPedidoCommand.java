package com.SITFAI_CORE_ERP_TIENDA.orders.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CrearPedidoCommand(
        UUID empresaId,
        UUID clienteId,
        List<LineaComando> lineas
) {
    public record LineaComando(
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario
    ) {}
}
