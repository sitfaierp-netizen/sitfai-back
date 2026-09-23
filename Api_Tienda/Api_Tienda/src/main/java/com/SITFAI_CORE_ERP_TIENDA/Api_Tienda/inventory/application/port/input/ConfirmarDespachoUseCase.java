package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConfirmarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DespachoResponse;

/**
 * Driving Input Port: Caso de Uso para la confirmación de despachos en Logística de Salida.
 * <p>
 * Regla 1 (Clean Architecture): Interfaz pura en Application Port Input.
 */
public interface ConfirmarDespachoUseCase {

    /**
     * Procesa la confirmación de salida de mercancía originada en un Pedido.
     *
     * @param command Datos del despacho y sus líneas.
     * @return Respuesta con el resultado del despacho confirmado.
     */
    DespachoResponse confirmarDespacho(ConfirmarDespachoCommand command);
}
