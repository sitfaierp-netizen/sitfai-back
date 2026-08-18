package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto;

import java.util.List;
import java.util.UUID;

public record PlanificarDespachoCommand(
        UUID empresaId,
        UUID pedidoOrigenId,
        String direccionLocal,
        String ciudad,
        String codigoPostal,
        List<LineaDespachoCommand> lineas
) {
}
