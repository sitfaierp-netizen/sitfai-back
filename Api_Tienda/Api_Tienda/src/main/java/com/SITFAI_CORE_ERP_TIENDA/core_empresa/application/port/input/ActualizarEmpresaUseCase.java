package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.ActualizarEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;

public interface ActualizarEmpresaUseCase {
    EmpresaResponse ejecutar(ActualizarEmpresaCommand command);
}
