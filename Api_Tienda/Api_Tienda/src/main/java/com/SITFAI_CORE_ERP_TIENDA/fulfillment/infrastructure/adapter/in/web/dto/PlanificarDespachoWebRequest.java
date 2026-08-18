package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record PlanificarDespachoWebRequest(
        UUID pedidoOrigenId,
        String direccionLocal,
        String ciudad,
        String codigoPostal,
        List<LineaDespachoWebRequest> lineas
) {
    public record LineaDespachoWebRequest(
            UUID productoId,
            int cantidadSolicitada
    ) {}
}
