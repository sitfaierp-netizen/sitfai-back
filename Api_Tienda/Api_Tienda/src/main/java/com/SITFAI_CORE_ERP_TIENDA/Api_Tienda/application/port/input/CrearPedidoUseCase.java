package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;

/**
 * Driving Port (API): Caso de uso para inicializar un nuevo Pedido en estado CREADO.
 */
public interface CrearPedidoUseCase {
    PedidoResponse ejecutar(CrearPedidoCommand command);
}
