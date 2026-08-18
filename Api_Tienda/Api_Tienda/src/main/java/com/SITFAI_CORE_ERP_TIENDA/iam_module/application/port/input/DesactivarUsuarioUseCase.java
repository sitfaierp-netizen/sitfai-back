package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.DesactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;

/**
 * Driving Port / Input Port: Caso de Uso para desactivar un Usuario existente.
 */
public interface DesactivarUsuarioUseCase {
    UsuarioResponse ejecutar(DesactivarUsuarioCommand command);
}
