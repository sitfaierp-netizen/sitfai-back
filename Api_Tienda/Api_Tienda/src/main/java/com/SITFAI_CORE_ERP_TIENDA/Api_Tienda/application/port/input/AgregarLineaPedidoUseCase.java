package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;

/**
 * Driving Port: Caso de uso para agregar o incrementar ítems en un Pedido existente.
 */
public interface AgregarLineaPedidoUseCase {

    PedidoResponse ejecutar(AgregarLineaCommand command);
}
