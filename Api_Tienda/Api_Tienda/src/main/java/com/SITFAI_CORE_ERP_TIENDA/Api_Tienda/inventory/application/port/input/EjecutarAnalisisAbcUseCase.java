package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.AnalisisAbcResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.EjecutarAnalisisAbcCommand;

/**
 * Driving Input Port: Caso de Uso para la ejecución masiva del Análisis ABC de Inventario.
 * <p>
 * Regla 1 (Clean Architecture): Interfaz pura en Application Layer.
 */
public interface EjecutarAnalisisAbcUseCase {

    /**
     * Orquesta el análisis ABC sobre los productos de la bodega indicada bajo el tenant autenticado.
     *
     * @param command Comando con la bodega objetivo.
     * @return Resumen consolidado de la reclasificación efectuada.
     */
    AnalisisAbcResponse ejecutar(EjecutarAnalisisAbcCommand command);
}
