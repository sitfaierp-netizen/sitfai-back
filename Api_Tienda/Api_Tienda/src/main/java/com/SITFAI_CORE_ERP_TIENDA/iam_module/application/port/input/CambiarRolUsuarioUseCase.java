package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CambiarRolUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;

/**
 * Driving Port / Input Port: Caso de Uso para modificar el rol de un Usuario.
 */
public interface CambiarRolUsuarioUseCase {
    UsuarioResponse ejecutar(CambiarRolUsuarioCommand command);
}
