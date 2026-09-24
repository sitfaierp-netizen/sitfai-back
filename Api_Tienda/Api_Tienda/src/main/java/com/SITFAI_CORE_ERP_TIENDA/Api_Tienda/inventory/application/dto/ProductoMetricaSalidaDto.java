package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO de Consulta: Representa las métricas consolidadas de salida de un producto en un período.
 */
public record ProductoMetricaSalidaDto(
        UUID productoId,
        int frecuenciaSalida,
        BigDecimal valorTotalDespachado
) {
    public ProductoMetricaSalidaDto {
        Objects.requireNonNull(productoId, "ProductoMetricaSalidaDto: productoId no puede ser nulo.");
        Objects.requireNonNull(valorTotalDespachado, "ProductoMetricaSalidaDto: valorTotalDespachado no puede ser nulo.");
        if (frecuenciaSalida < 0) {
            throw new IllegalArgumentException("frecuenciaSalida no puede ser negativa.");
        }
    }
}
