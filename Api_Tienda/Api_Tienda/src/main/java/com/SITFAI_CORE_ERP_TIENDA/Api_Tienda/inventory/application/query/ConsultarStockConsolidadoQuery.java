package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query;

import java.util.UUID;

/**
 * Query DTO (CQRS): Comando de lectura inmutable.
 * Exige estrictamente el empresaId (tenant) por regla MT-01 y MT-04.
 */
public record ConsultarStockConsolidadoQuery(
        UUID empresaId,
        UUID bodegaId,
        UUID productoId
) {
    public ConsultarStockConsolidadoQuery {
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId es obligatorio para consultar stock consolidado (Zero Trust)");
        }
    }
}
