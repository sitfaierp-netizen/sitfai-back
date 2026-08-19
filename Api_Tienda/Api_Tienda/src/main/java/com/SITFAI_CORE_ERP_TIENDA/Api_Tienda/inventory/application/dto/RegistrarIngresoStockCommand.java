package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Comando para registrar un ingreso de stock.
 * No incluye empresaId por seguridad (se extrae del token vía TenantProviderPort).
 */
public record RegistrarIngresoStockCommand(
        UUID bodegaId,
        UUID productoId,
        BigDecimal cantidad,
        String loteId, // Opcional
        Instant fechaCaducidad, // Opcional
        String docFuenteTipo,
        String docFuenteNumero
) {
}
