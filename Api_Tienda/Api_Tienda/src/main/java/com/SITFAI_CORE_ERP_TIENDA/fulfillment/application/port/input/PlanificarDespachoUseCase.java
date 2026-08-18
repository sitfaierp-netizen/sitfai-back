package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.PlanificarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;

public interface PlanificarDespachoUseCase {
    OrdenDespachoResponse ejecutar(PlanificarDespachoCommand command);
}
