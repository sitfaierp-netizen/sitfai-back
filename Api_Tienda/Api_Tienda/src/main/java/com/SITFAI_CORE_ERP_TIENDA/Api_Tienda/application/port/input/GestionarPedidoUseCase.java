package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;

/**
 * Driving Input Port: Caso de Uso para la gestión del ciclo de vida del Pedido (Creación y Confirmación).
 * <p>
 * Regla REGLA-1: Interfaz pura en Application Layer.
 */
public interface GestionarPedidoUseCase {

    /**
     * Orquesta la creación de un nuevo pedido de venta.
     */
    PedidoResponse crear(CrearPedidoCommand command);

    /**
     * Orquesta la confirmación formal del pedido, disparando las reservas de inventario.
     */
    PedidoResponse confirmar(ConfirmarPedidoCommand command);
}
