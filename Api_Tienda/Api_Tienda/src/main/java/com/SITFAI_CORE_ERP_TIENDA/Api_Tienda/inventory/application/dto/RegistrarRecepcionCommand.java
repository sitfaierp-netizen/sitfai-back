package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.List;
import java.util.UUID;

public record RegistrarRecepcionCommand(
        UUID bodegaDestinoId,
        UUID ordenCompraOrigenId,
        List<LineaRecepcionDto> lineas
) {
}
