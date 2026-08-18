package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO: Parámetros para cancelar un Pedido.
 * <p>
 * Record puro de Java 25 — inmutable, sin anotaciones de Spring ni Jackson (REGLA 1, REGLA 2).
 */
public record CancelarPedidoCommand(
        UUID empresaId,
        UUID pedidoId,
        String motivo
) {
    public CancelarPedidoCommand {
        Objects.requireNonNull(empresaId, "CancelarPedidoCommand: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(pedidoId, "CancelarPedidoCommand: pedidoId es obligatorio.");
        if (motivo == null || motivo.isBlank()) {
            motivo = "Cancelación sin motivo especificado";
        }
    }
}
