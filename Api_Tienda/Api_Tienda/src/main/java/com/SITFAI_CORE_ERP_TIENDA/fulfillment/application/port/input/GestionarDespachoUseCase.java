package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.ConfirmarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.EmpacarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.IniciarPickingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;

public interface GestionarDespachoUseCase {
    OrdenDespachoResponse iniciarPicking(IniciarPickingCommand command);
    OrdenDespachoResponse empacar(EmpacarDespachoCommand command);
    OrdenDespachoResponse confirmar(ConfirmarDespachoCommand command);
}
