package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovimientoIngresoDto(
        UUID productoId,
        BigDecimal cantidad,
        String codigoLote,
        Instant fechaCaducidad
) {
    public MovimientoIngresoDto {
        if (productoId == null) throw new IllegalArgumentException("productoId es obligatorio");
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("cantidad debe ser mayor a 0");
    }
}
