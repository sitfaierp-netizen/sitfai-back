package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO: Parámetros para agregar una línea de detalle a un Pedido.
 * <p>
 * Record puro de Java 25 — inmutable, sin anotaciones de Spring ni Jackson (REGLA 1, REGLA 2).
 */
public record AgregarLineaPedidoCommand(
        UUID empresaId,
        UUID pedidoId,
        UUID productoId,
        int cantidad,
        BigDecimal precioUnitario,
        String moneda
) {
    public AgregarLineaPedidoCommand {
        Objects.requireNonNull(empresaId, "AgregarLineaPedidoCommand: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(pedidoId, "AgregarLineaPedidoCommand: pedidoId es obligatorio.");
        Objects.requireNonNull(productoId, "AgregarLineaPedidoCommand: productoId es obligatorio.");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("AgregarLineaPedidoCommand: la cantidad debe ser mayor a 0.");
        }
        Objects.requireNonNull(precioUnitario, "AgregarLineaPedidoCommand: el precio unitario no puede ser null.");
        if (precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("AgregarLineaPedidoCommand: el precio unitario no puede ser negativo.");
        }
        if (moneda == null || moneda.isBlank()) {
            moneda = "USD";
        } else {
            moneda = moneda.trim().toUpperCase();
        }
    }
}
