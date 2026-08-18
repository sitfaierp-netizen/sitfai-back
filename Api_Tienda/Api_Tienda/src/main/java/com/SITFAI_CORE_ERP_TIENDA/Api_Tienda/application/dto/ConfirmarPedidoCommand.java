package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.util.UUID;

/**
 * Command DTO: Parámetros para confirmar un Pedido.
 * <p>
 * Record puro de Java 25 — inmutable, sin anotaciones de Spring ni de Jackson (REGLA-1, REGLA-2).
 */
public record ConfirmarPedidoCommand(
        UUID empresaId,
        UUID pedidoId
) {
    public ConfirmarPedidoCommand {
        if (empresaId == null) {
            throw new IllegalArgumentException("ConfirmarPedidoCommand: empresaId es obligatorio.");
        }
        if (pedidoId == null) {
            throw new IllegalArgumentException("ConfirmarPedidoCommand: pedidoId es obligatorio.");
        }
    }
}
