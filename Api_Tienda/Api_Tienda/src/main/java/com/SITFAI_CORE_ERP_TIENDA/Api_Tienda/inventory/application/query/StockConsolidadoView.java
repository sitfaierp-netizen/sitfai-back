package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * View DTO (CQRS Read Model): Proyección plana desnormalizada.
 * No contiene lógica de negocio, optimizado exclusivamente para lectura rápida.
 */
public record StockConsolidadoView(
        UUID empresaId,
        UUID bodegaId,
        UUID productoId,
        BigDecimal cantidadTotal,
        Instant ultimaActualizacion
) {
}
