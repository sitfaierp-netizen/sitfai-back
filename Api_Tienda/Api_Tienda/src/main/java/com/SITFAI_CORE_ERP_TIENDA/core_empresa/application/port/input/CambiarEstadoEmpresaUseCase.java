package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.DarDeBajaEmpresaCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.EmpresaResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SuspenderEmpresaCommand;

import java.util.UUID;

/**
 * Puerto de Entrada (Driving Port): Caso de Uso para transiciones de estado de una Empresa (EMP-04, EMP-06, EMP-07).
 */
public interface CambiarEstadoEmpresaUseCase {

    EmpresaResponse suspender(SuspenderEmpresaCommand command);

    EmpresaResponse darDeBaja(DarDeBajaEmpresaCommand command);

    EmpresaResponse reactivar(UUID empresaId);
}
