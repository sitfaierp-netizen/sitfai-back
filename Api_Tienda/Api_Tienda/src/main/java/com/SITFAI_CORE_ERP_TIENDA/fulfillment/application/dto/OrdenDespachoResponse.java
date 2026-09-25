package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto;

import java.util.List;
import java.util.UUID;

public record OrdenDespachoResponse(
        UUID id,
        UUID empresaId,
        UUID pedidoId,
        UUID bodegaId,
        String estado,
        List<LineaDespachoResponse> lineas
) {}
