package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Web Response DTO: Representación HTTP consolidada de un Conteo Cíclico.
 */
public record ConteoCiclicoWebResponse(
        UUID id,
        UUID empresaId,
        UUID bodegaId,
        String estado,
        LocalDate fechaProgramada,
        int totalLineas,
        int totalDiscrepancias,
        List<DetalleConteoWebResponse> detalles
) {
}
