package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.CrearProveedorCommand;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.ProveedorResponse;

/** Driving Port para crear un proveedor. */
public interface CrearProveedorUseCase {
    ProveedorResponse crear(CrearProveedorCommand command);
}
