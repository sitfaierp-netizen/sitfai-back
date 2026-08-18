package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RegistrarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;

/**
 * Driving Port / Input Port: Caso de Uso para registrar un nuevo Usuario en un Tenant.
 */
public interface RegistrarUsuarioUseCase {
    UsuarioResponse ejecutar(RegistrarUsuarioCommand command);
}
