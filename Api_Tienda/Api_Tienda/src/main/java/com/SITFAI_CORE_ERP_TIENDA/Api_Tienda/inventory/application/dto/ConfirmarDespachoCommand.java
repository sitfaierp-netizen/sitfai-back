package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando inmutable (DTO de Aplicación) para confirmar la salida física de mercancía (Outbound Logistics).
 * <p>
 * Regla 1 (Clean Architecture): Libre de anotaciones de frameworks técnicos (JPA, Jackson, Spring).
 * Regla BOD-04: Referencia ineludible al Pedido de origen como documento fuente.
 */
public record ConfirmarDespachoCommand(
        UUID pedidoId,
        UUID bodegaId,
        List<LineaDespachoCommand> lineas
) {
    public ConfirmarDespachoCommand {
        Objects.requireNonNull(pedidoId, "ConfirmarDespachoCommand: pedidoId es obligatorio (BOD-04).");
        lineas = lineas != null ? Collections.unmodifiableList(lineas) : Collections.emptyList();
    }

    public record LineaDespachoCommand(
            UUID productoId,
            BigDecimal cantidad
    ) {
        public LineaDespachoCommand {
            Objects.requireNonNull(productoId, "LineaDespachoCommand: productoId es obligatorio.");
            Objects.requireNonNull(cantidad, "LineaDespachoCommand: cantidad es obligatoria.");
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("LineaDespachoCommand: cantidad debe ser estrictamente positiva.");
            }
        }
    }
}
