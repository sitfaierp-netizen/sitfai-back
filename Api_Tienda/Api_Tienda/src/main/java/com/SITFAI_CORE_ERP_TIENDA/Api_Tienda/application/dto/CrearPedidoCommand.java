package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto;

import java.util.UUID;

/**
 * Command DTO: Parámetros requeridos para inicializar un borrador de Pedido.
 * <p>
 * Record puro de Java 21 — inmutable, sin anotaciones de Spring ni de Jackson (REGLA-1, REGLA-2).
 */
public record CrearPedidoCommand(
        UUID empresaId,
        UUID clienteId
) {
    public CrearPedidoCommand {
        if (empresaId == null) {
            throw new IllegalArgumentException("CrearPedidoCommand: empresaId es obligatorio.");
        }
        if (clienteId == null) {
            throw new IllegalArgumentException("CrearPedidoCommand: clienteId es obligatorio.");
        }
    }
}
