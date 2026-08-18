package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

/**
 * Query para consultar el stock de un Producto en una Bodega específica.
 * <p>
 * Record puro — inmutable (Java 25). Representa una operación de solo lectura
 * (no genera Domain Events ni modifica estado).
 * <p>
 * Reglas validadas: MT-01 (empresaId obligatorio para aislamiento de tenant),
 * REGLA-2 (dto en application/dto).
 *
 * @param empresaId  UUID del tenant — extraído del JWT (MT-01, MT-02).
 * @param bodegaId   UUID de la Bodega a consultar.
 * @param productoId UUID del Producto cuyo stock se quiere conocer.
 */
public record ConsultarStockQuery(
        String empresaId,
        String bodegaId,
        String productoId
) {
    public ConsultarStockQuery {
        if (empresaId == null || empresaId.isBlank())
            throw new IllegalArgumentException("ConsultarStockQuery: empresaId es obligatorio (MT-01).");
        if (bodegaId == null || bodegaId.isBlank())
            throw new IllegalArgumentException("ConsultarStockQuery: bodegaId es obligatorio.");
        if (productoId == null || productoId.isBlank())
            throw new IllegalArgumentException("ConsultarStockQuery: productoId es obligatorio.");
    }
}
