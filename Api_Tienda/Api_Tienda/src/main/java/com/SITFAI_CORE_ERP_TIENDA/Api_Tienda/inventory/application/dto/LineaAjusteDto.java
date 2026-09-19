package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LineaAjusteDto(
        UUID productoId,
        String codigoLote,
        LocalDate fechaCaducidad,
        BigDecimal diferencia
) {
    public LineaAjusteDto {
        if (productoId == null) {
            throw new IllegalArgumentException("El productoId es obligatorio.");
        }
        if (diferencia == null || diferencia.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("La diferencia no puede ser nula ni cero.");
        }
    }
}
