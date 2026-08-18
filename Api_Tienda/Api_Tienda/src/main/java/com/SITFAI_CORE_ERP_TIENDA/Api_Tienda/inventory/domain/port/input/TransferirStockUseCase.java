package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.TransferirStockCommand;

/**
 * Driving Port (Puerto de Entrada): Define el caso de uso para transferir stock entre bodegas.
 */
public interface TransferirStockUseCase {
    
    /**
     * Ejecuta la transferencia de stock basada en los datos del comando.
     * @param command Contiene origen, destino, producto, cantidad y documento fuente.
     */
    void ejecutar(TransferirStockCommand command);
}
