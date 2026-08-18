package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Web Request DTO para configurar el punto de reorden de un producto.
 * Puro Java, sin anotaciones de validación complejas para respetar el aislamiento de capas.
 */
public record ConfigurarPuntoReordenWebRequest(
        UUID productoId,
        BigDecimal puntoReorden
) {}
