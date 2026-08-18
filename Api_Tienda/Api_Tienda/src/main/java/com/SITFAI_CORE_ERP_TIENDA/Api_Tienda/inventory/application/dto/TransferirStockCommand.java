package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command DTO: Transporta los datos necesarios para registrar una transferencia de stock.
 * <p>
 * Puro Java 25. No contiene validaciones de infraestructura (sin javax.validation).
 */
public record TransferirStockCommand(
    UUID empresaId,
    UUID bodegaOrigenId,
    UUID bodegaDestinoId,
    UUID productoId,
    BigDecimal cantidad,
    String documentoFuente
) {}
