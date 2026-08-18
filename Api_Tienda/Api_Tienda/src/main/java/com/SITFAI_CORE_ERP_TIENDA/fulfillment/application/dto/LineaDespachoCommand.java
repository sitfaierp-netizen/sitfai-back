package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto;

import java.util.UUID;

public record LineaDespachoCommand(
        UUID productoId,
        int cantidadSolicitada
) {
}
