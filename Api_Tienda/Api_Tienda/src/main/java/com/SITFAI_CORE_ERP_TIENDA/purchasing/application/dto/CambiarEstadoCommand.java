package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

public record CambiarEstadoCommand(
        UUID ordenCompraId,
        UUID empresaId,
        String nuevoEstado // "EMITIDA", "RECIBIDA", "CANCELADA"
) {
    public CambiarEstadoCommand {
        if (ordenCompraId == null) throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        if (empresaId == null) throw new IllegalArgumentException("El EmpresaId es obligatorio.");
        if (nuevoEstado == null || nuevoEstado.isBlank()) throw new IllegalArgumentException("El nuevo estado es obligatorio.");
    }
}
