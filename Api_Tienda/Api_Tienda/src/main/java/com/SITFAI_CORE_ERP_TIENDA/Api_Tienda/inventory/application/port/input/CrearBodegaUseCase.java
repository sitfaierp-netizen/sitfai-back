package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CrearBodegaCommand;

/** Driving port for warehouse creation. */
public interface CrearBodegaUseCase {
    BodegaResponse ejecutar(CrearBodegaCommand command);
}
