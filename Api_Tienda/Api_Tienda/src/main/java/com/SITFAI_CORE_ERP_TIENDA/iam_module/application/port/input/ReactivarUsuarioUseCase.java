package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.ReactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;

/**
 * Driving Port / Input Port: Caso de Uso para reactivar un Usuario inactivo.
 */
public interface ReactivarUsuarioUseCase {
    UsuarioResponse ejecutar(ReactivarUsuarioCommand command);
}
