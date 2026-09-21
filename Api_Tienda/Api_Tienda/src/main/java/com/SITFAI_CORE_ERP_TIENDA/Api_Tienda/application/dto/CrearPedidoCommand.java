package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CrearPedidoCommand(
        UUID empresaId,
        UUID clienteId,
        List<LineaComando> lineas
) {
    public CrearPedidoCommand {
        if (empresaId == null) throw new IllegalArgumentException("empresaId es obligatorio");
        if (clienteId == null) throw new IllegalArgumentException("clienteId es obligatorio");
        if (lineas == null || lineas.isEmpty()) throw new IllegalArgumentException("Debe contener al menos una línea");
    }

    public record LineaComando(
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario
    ) {}
}
