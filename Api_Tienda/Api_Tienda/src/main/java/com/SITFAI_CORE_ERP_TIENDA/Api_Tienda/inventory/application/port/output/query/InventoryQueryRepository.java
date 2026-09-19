package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.query;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.MovimientoKardexView;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.StockDisponibleView;

import java.util.List;

/**
 * Puerto de Salida (Driven Port) para ejecutar consultas directas de CQRS (Lado Lectura) 
 * sin pasar por JPA Entities pesadas.
 */
public interface InventoryQueryRepository {

    /**
     * Consulta la vista materializada o tabla desnormalizada para retornar el stock.
     */
    List<StockDisponibleView> findStockByEmpresaAndBodega(String empresaId, String bodegaId);

    /**
     * Consulta los movimientos históricos (kárdex).
     */
    List<MovimientoKardexView> findKardexByEmpresaBodegaAndProducto(String empresaId, String bodegaId, String productoId);
}
