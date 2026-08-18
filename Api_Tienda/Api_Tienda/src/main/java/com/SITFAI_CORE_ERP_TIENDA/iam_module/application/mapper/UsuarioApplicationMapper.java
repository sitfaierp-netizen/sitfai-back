package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;

import java.util.List;

/**
 * Clase utilitaria estática para mapear entre entidades del Agregado Usuario y DTOs de Aplicación.
 */
public final class UsuarioApplicationMapper {

    private UsuarioApplicationMapper() {
        // Constructor privado para clase utilitaria estática
    }

    public static UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId().valor(),
                usuario.getEmpresaId().valor(),
                usuario.getUsername().valor(),
                usuario.getEmail().valor(),
                usuario.getRol().name(),
                usuario.getEstado().name(),
                usuario.getCreadoEn(),
                usuario.getActualizadoEn()
        );
    }

    public static List<UsuarioResponse> toResponseList(List<Usuario> usuarios) {
        if (usuarios == null) {
            return List.of();
        }
        return usuarios.stream()
                .map(UsuarioApplicationMapper::toResponse)
                .toList();
    }
}
