package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.ConsultarStockConsolidadoQuery;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.StockConsolidadoView;

import java.util.List;

/**
 * Puerto de Salida para el Modelo de Lectura (CQRS Read Port).
 * Solo devuelve View DTOs, NUNCA devuelve Aggregates del modelo de escritura.
 */
public interface StockQueryRepository {
    
    List<StockConsolidadoView> consultarStockConsolidado(ConsultarStockConsolidadoQuery query);
    
}
