package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.EstadoUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.RolUsuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

import java.util.List;

/**
 * Mapper para traducción bidireccional entre el Agregado de Dominio Usuario y la Entidad JPA.
 */
public final class UsuarioPersistenceMapper {

    private UsuarioPersistenceMapper() {
    }

    public static UsuarioJpaEntity toJpaEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioJpaEntity(
                usuario.getId().valor().toString(),
                usuario.getEmpresaId().valor().toString(),
                usuario.getUsername().valor(),
                usuario.getEmail().valor(),
                usuario.getRol().name(),
                usuario.getEstado().name(),
                usuario.getCreadoEn(),
                usuario.getActualizadoEn(),
                usuario.isActivo(),
                usuario.getDeletedAt(),
                usuario.getDeletedBy()
        );
    }

    public static Usuario toDomainEntity(UsuarioJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Usuario.reconstituir(
                UsuarioId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                Username.de(entity.getUsername()),
                Email.de(entity.getEmail()),
                RolUsuario.valueOf(entity.getRol()),
                EstadoUsuario.valueOf(entity.getEstado()),
                entity.getCreadoEn(),
                entity.getActualizadoEn(),
                entity.isActivo(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public static List<Usuario> toDomainEntityList(List<UsuarioJpaEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(UsuarioPersistenceMapper::toDomainEntity)
                .toList();
    }
}
