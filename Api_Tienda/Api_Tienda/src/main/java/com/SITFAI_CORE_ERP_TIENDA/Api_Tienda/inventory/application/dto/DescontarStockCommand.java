package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Comando para descontar stock (aplicando lógica FEFO autónoma del agregado).
 * No incluye empresaId por seguridad (se extrae del token vía TenantProviderPort).
 */
public record DescontarStockCommand(
        UUID bodegaId,
        UUID productoId,
        BigDecimal cantidad,
        String docFuenteTipo,
        String docFuenteNumero,
        UUID empresaId
) {
    public DescontarStockCommand(
            UUID bodegaId,
            UUID productoId,
            BigDecimal cantidad,
            String docFuenteTipo,
            String docFuenteNumero) {
        this(bodegaId, productoId, cantidad, docFuenteTipo, docFuenteNumero, null);
    }
}
