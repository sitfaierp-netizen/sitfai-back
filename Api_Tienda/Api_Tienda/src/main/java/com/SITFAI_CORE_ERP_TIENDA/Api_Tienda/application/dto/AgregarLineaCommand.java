package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command DTO: Parámetros para agregar un ítem o línea de detalle a un Pedido.
 * <p>
 * Record puro de Java 25 — inmutable, sin anotaciones de Spring ni de Jackson (REGLA-1, REGLA-2).
 */
public record AgregarLineaCommand(
        UUID empresaId,
        UUID pedidoId,
        UUID productoId,
        int cantidad,
        BigDecimal precioUnitario,
        String moneda
) {
    public AgregarLineaCommand {
        if (empresaId == null) {
            throw new IllegalArgumentException("AgregarLineaCommand: empresaId es obligatorio.");
        }
        if (pedidoId == null) {
            throw new IllegalArgumentException("AgregarLineaCommand: pedidoId es obligatorio.");
        }
        if (productoId == null) {
            throw new IllegalArgumentException("AgregarLineaCommand: productoId es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("AgregarLineaCommand: la cantidad debe ser mayor a 0.");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("AgregarLineaCommand: el precio unitario no puede ser null ni negativo.");
        }
        if (moneda == null || moneda.isBlank()) {
            moneda = "USD";
        } else {
            moneda = moneda.trim().toUpperCase();
        }
    }
}
