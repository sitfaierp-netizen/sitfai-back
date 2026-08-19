package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO de SolicitudAbastecimiento.
 * Expone únicamente datos primitivos — el Dominio nunca cruza la frontera HTTP.
 */
public record SolicitudResponse(
        UUID id,
        UUID empresaId,
        UUID bodegaId,
        String estado,
        Instant creadoEn,
        List<LineaSolicitudResponse> lineas
) {
    public record LineaSolicitudResponse(
            UUID productoId,
            java.math.BigDecimal cantidadSolicitada
    ) {}
}
