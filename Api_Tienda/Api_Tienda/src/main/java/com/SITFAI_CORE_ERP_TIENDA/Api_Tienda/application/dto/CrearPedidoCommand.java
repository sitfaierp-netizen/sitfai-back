package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO: Datos necesarios para la creación de un nuevo pedido de venta.
 * Inmutable y libre de dependencias de frameworks (REGLA-1).
 */
public record CrearPedidoCommand(
        UUID empresaId,
        UUID clienteId,
        List<LineaComando> lineas
) {
    public CrearPedidoCommand(UUID clienteId, List<LineaComando> lineas) {
        this(null, clienteId, lineas);
    }

    public CrearPedidoCommand {
        Objects.requireNonNull(clienteId, "CrearPedidoCommand: clienteId es obligatorio.");
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("CrearPedidoCommand: debe contener al menos una línea.");
        }
        lineas = List.copyOf(lineas);
    }

    public record LineaComando(
            UUID productoId,
            int cantidad,
            BigDecimal precioUnitario
    ) {
        public LineaComando {
            Objects.requireNonNull(productoId, "LineaComando: productoId es obligatorio.");
            if (cantidad <= 0) {
                throw new IllegalArgumentException("LineaComando: cantidad debe ser positiva (> 0).");
            }
            Objects.requireNonNull(precioUnitario, "LineaComando: precioUnitario es obligatorio.");
        }
    }
}
