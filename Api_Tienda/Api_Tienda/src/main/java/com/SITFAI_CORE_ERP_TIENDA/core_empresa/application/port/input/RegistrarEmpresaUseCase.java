package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.RegistrarEmpresaCommand;

public interface RegistrarEmpresaUseCase {
    EmpresaResponse ejecutar(RegistrarEmpresaCommand command);
}
