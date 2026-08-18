package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.CompletarPackingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;

public interface CompletarPackingUseCase {
    OrdenDespachoResponse ejecutar(CompletarPackingCommand command);
}
