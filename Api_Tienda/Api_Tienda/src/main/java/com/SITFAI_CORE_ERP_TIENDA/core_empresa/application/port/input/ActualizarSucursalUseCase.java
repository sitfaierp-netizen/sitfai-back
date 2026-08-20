package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.ActualizarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;

public interface ActualizarSucursalUseCase {
    SucursalResponse ejecutar(ActualizarSucursalCommand command);
}
