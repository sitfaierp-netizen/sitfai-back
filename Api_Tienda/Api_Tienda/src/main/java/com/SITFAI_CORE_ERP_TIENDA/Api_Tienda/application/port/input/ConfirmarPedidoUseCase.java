package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;

/**
 * Driving Port (API): Caso de uso para confirmar un Pedido y emitir el evento PedidoConfirmadoEvent.
 */
public interface ConfirmarPedidoUseCase {
    PedidoResponse ejecutar(ConfirmarPedidoCommand command);
}
