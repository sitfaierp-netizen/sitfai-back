package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.in;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CrearRolCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RolResponse;
import java.util.List;

public interface GestionarRolesUseCase {
    List<RolResponse> listarRoles();
    RolResponse crearRol(CrearRolCommand command);
}
