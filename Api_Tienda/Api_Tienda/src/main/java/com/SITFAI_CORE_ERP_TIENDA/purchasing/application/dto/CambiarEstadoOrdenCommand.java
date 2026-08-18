package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

/**
 * DTO de Aplicación: Comando para cambiar el estado de una orden de compra (aprobar, recibir o cancelar).
 * Record puro de Java 25.
 */
public record CambiarEstadoOrdenCommand(
        UUID ordenCompraId,
        UUID empresaId,
        String nuevoEstado,
        String motivo
) {
}
