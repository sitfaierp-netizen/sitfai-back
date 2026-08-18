package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto;

import java.util.UUID;

public record LineaDespachoResponse(
        UUID id,
        UUID productoId,
        int cantidadSolicitada,
        int cantidadPreparada
) {
}
