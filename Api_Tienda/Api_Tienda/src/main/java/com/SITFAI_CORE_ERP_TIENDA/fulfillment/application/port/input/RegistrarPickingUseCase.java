package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.RegistrarPickingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;

public interface RegistrarPickingUseCase {
    OrdenDespachoResponse ejecutar(RegistrarPickingCommand command);
}
