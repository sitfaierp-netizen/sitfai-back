package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Command: Datos de entrada para crear una SolicitudAbastecimiento.
 * Record de Java 21 — inmutable por diseño. Sin anotaciones JPA ni Jackson.
 */
public record CrearSolicitudCommand(
        UUID empresaId,
        UUID bodegaId,
        List<LineaSolicitudCommand> lineas
) {
    public record LineaSolicitudCommand(
            UUID productoId,
            BigDecimal cantidadSolicitada
    ) {}
}
