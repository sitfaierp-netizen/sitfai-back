package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.List;
import java.util.UUID;

public record IncrementarStockCommand(
        UUID empresaId,
        UUID bodegaId,
        UUID documentoFuenteId,
        List<MovimientoIngresoDto> movimientos
) {
    public IncrementarStockCommand {
        if (empresaId == null) throw new IllegalArgumentException("empresaId es obligatorio");
        if (bodegaId == null) throw new IllegalArgumentException("bodegaId es obligatorio");
        if (documentoFuenteId == null) throw new IllegalArgumentException("documentoFuenteId es obligatorio");
        if (movimientos == null || movimientos.isEmpty()) throw new IllegalArgumentException("movimientos no puede ser vacio");
    }
}
