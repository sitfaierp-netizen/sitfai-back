package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO de Salida: Resumen consolidado del resultado de la ejecución del Análisis ABC.
 */
public record AnalisisAbcResponse(
        UUID bodegaId,
        UUID empresaId,
        int totalProductosClasificados,
        int totalCategoriaA,
        int totalCategoriaB,
        int totalCategoriaC,
        Instant fechaEjecucion,
        List<DetalleClasificacionDto> detalles
) {
    public record DetalleClasificacionDto(
            UUID productoId,
            String categoria,
            int frecuenciaSalida,
            BigDecimal valorTotalDespachado
    ) {}
}
