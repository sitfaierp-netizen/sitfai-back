package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Web Request DTO para transferencias de stock.
 * <p>
 * Puro Java 25, sin anotaciones de validación complejas para mantener 
 * el aislamiento (las validaciones se hacen en el Dominio y Value Objects).
 */
public record TransferirStockWebRequest(
    UUID bodegaOrigenId,
    UUID bodegaDestinoId,
    UUID productoId,
    BigDecimal cantidad,
    String documentoFuente
) {}
