package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.UUID;

public record CompletarRecepcionCommand(
    UUID recepcionId
) {
    public CompletarRecepcionCommand {
        if (recepcionId == null) throw new IllegalArgumentException("recepcionId es requerido");
    }
}
