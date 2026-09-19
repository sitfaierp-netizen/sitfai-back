package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.List;
import java.util.UUID;

public record RegistrarAjusteCommand(
        UUID bodegaId,
        String motivo,
        List<LineaAjusteDto> lineas
) {
    public RegistrarAjusteCommand {
        if (bodegaId == null) {
            throw new IllegalArgumentException("El bodegaId es obligatorio.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio.");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("El ajuste debe tener al menos una línea.");
        }
    }
}
