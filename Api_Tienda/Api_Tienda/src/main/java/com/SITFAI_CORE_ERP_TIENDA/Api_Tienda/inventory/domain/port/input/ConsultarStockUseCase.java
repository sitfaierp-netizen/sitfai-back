package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConsultarStockQuery;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.StockResponse;

/**
 * Puerto de Entrada (Driving Port) — Caso de Uso: Consultar Stock.
 * <p>
 * Operación de solo lectura. No genera Domain Events ni modifica estado del Agregado.
 * El {@code empresaId} garantiza que el tenant solo pueda consultar sus propias Bodegas (MT-02).
 * <p>
 * Reglas validadas: REGLA-1, REGLA-2, BOD-05 (stock >= 0 garantizado por Dominio), MT-01, MT-02.
 */
public interface ConsultarStockUseCase {

    /**
     * Consulta el stock actual de un Producto en una Bodega específica.
     *
     * @param query Query con bodegaId, productoId y empresaId.
     * @return      {@code StockResponse} con el stock actual (siempre {@code >= 0}).
     * @throws IllegalArgumentException Si la Bodega no existe o no pertenece al tenant.
     */
    StockResponse ejecutar(ConsultarStockQuery query);
}
