package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto;

import java.util.UUID;

public record RegistrarPickingCommand(
        UUID empresaId,
        UUID despachoId,
        UUID productoId,
        int cantidadPreparada
) {
}
