package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO inmutable de salida: Representación consolidada del estado y detalle de un Conteo Cíclico.
 */
public record ConteoCiclicoResponse(
        UUID id,
        UUID empresaId,
        UUID bodegaId,
        String estado,
        LocalDate fechaProgramada,
        int totalLineas,
        int totalDiscrepancias,
        List<DetalleConteoResponse> detalles
) {
}
