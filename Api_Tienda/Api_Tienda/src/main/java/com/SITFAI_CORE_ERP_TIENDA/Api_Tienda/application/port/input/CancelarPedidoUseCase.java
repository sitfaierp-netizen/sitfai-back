package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;

/**
 * Driving Port (API): Caso de uso para cancelar un Pedido indicando el motivo.
 */
public interface CancelarPedidoUseCase {
    PedidoResponse ejecutar(CancelarPedidoCommand command);
}
