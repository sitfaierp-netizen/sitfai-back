package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO: Parámetros para confirmar un Pedido.
 * Inmutable y libre de dependencias de frameworks (REGLA-1).
 */
public record ConfirmarPedidoCommand(
        UUID empresaId,
        UUID pedidoId
) {
    public ConfirmarPedidoCommand(UUID pedidoId) {
        this(null, pedidoId);
    }

    public ConfirmarPedidoCommand {
        Objects.requireNonNull(pedidoId, "ConfirmarPedidoCommand: pedidoId es obligatorio.");
    }
}
