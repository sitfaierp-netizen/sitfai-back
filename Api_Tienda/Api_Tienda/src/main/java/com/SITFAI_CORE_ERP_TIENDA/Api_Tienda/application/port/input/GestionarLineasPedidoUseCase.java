package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.RemoverLineaPedidoCommand;

/**
 * Driving Port (API): Caso de uso para agregar y remover líneas de detalle en un Pedido.
 */
public interface GestionarLineasPedidoUseCase {

    PedidoResponse agregarLinea(AgregarLineaPedidoCommand command);

    PedidoResponse removerLinea(RemoverLineaPedidoCommand command);
}
