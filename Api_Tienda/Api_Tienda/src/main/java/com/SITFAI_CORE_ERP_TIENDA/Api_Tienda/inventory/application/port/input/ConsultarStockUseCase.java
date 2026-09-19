package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.MovimientoKardexView;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.StockDisponibleView;

import java.util.List;

/**
 * Puerto de Entrada (Driving Port) para las consultas de CQRS del módulo Inventory.
 */
public interface ConsultarStockUseCase {
    
    /**
     * Obtiene el stock disponible de todos los productos en una bodega.
     * El empresaId se extrae del contexto de seguridad internamente.
     */
    List<StockDisponibleView> consultarStockBodega(String bodegaId);
    
    /**
     * Obtiene el kárdex (historial de movimientos) de un producto en una bodega.
     * El empresaId se extrae del contexto de seguridad internamente.
     */
    List<MovimientoKardexView> consultarKardex(String bodegaId, String productoId);
}
