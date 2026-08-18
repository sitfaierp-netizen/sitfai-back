package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command DTO para configurar el punto de reorden (Replenishment) de un producto en una bodega.
 * Puro Java 25.
 */
public record ConfigurarPuntoReordenCommand(
        UUID empresaId,
        UUID bodegaId,
        UUID productoId,
        BigDecimal puntoReorden
) {}
