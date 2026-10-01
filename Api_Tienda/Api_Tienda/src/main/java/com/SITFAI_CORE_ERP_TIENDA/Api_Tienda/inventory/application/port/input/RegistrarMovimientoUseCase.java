package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;

/** Driving port for tenant-scoped inventory movements. */
public interface RegistrarMovimientoUseCase {
    MovimientoResponse ejecutar(RegistrarMovimientoCommand command);
}
