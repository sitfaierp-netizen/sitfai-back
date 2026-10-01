package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CambiarRolUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.DesactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.ReactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RegistrarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.CambiarRolUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.DesactivarUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.ReactivarUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;

import java.util.UUID;

/**
 * Traduce peticiones HTTP entrantes a Commands de la Capa de Aplicación.
 */
public final class UsuarioWebMapper {

    private UsuarioWebMapper() {
    }

    public static RegistrarUsuarioCommand toCommand(UUID empresaId, RegistrarUsuarioRequest request) {
        if (request == null) {
            return null;
        }
        return new RegistrarUsuarioCommand(
                request.id(),
                empresaId,
                request.username(),
                request.email(),
                request.rol()
        );
    }

    public static DesactivarUsuarioCommand toCommand(UUID empresaId, UUID id, DesactivarUsuarioRequest request) {
        if (request == null) {
            return null;
        }
        return new DesactivarUsuarioCommand(
                id,
                empresaId,
                request.motivo()
        );
    }

    public static ReactivarUsuarioCommand toCommand(UUID empresaId, UUID id, ReactivarUsuarioRequest request) {
        if (request == null) {
            return null;
        }
        return new ReactivarUsuarioCommand(
                id,
                empresaId
        );
    }

    public static CambiarRolUsuarioCommand toCommand(UUID empresaId, UUID id, CambiarRolUsuarioRequest request) {
        if (request == null) {
            return null;
        }
        return new CambiarRolUsuarioCommand(
                id,
                empresaId,
                request.nuevoRol()
        );
    }
}
