package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RecepcionMercanciaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RecepcionarMercanciaCommand;

/**
 * Driving Input Port: Caso de Uso para la Recepción Física de Mercancía en Bodega (Inbound Logistics).
 * <p>
 * Regla BOD-04: Exige un documento fuente traceable (ej. Orden de Compra).
 * Regla MT-01: Opera bajo el aislamiento estricto del tenant autenticado.
 */
public interface RecepcionarMercanciaUseCase {

    /**
     * Procesa la entrada física de mercancía amparada en una Orden de Compra.
     *
     * @param command Comando con los lotes recibidos y la referencia de la orden.
     * @return Respuesta con los detalles de la recepción procesada.
     */
    RecepcionMercanciaResponse recepcionar(RecepcionarMercanciaCommand command);
}
