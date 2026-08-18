package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record RegistrarPickingWebRequest(
        UUID productoId,
        int cantidadPreparada
) {
}
