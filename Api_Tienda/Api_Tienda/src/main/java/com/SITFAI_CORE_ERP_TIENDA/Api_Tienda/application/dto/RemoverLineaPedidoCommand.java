package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO: Parámetros para remover una línea de detalle de un Pedido.
 * <p>
 * Record puro de Java 25 — inmutable, sin anotaciones de Spring ni Jackson (REGLA 1, REGLA 2).
 */
public record RemoverLineaPedidoCommand(
        UUID empresaId,
        UUID pedidoId,
        UUID lineaId
) {
    public RemoverLineaPedidoCommand {
        Objects.requireNonNull(empresaId, "RemoverLineaPedidoCommand: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(pedidoId, "RemoverLineaPedidoCommand: pedidoId es obligatorio.");
        Objects.requireNonNull(lineaId, "RemoverLineaPedidoCommand: lineaId es obligatorio.");
    }
}
