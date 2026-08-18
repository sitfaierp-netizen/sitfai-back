package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioRepository;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Driven Adapter: Implementación del puerto UsuarioRepository usando Spring Data JPA.
 * Garantiza el aislamiento multitenant según la regla MT-01.
 */
@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioJpaAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository no puede ser null.");
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity entity = UsuarioPersistenceMapper.toJpaEntity(usuario);
        UsuarioJpaEntity guardado = jpaRepository.save(entity);
        return UsuarioPersistenceMapper.toDomainEntity(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorId(EmpresaId empresaId, UsuarioId id) {
        return jpaRepository.findByEmpresaIdAndId(
                empresaId.valor().toString(),
                id.valor().toString()
        ).map(UsuarioPersistenceMapper::toDomainEntity);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(EmpresaId empresaId, Username username) {
        return jpaRepository.findByEmpresaIdAndUsername(
                empresaId.valor().toString(),
                username.valor()
        ).map(UsuarioPersistenceMapper::toDomainEntity);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(EmpresaId empresaId, Email email) {
        return jpaRepository.findByEmpresaIdAndEmail(
                empresaId.valor().toString(),
                email.valor()
        ).map(UsuarioPersistenceMapper::toDomainEntity);
    }

    @Override
    public boolean existePorUsername(EmpresaId empresaId, Username username) {
        return jpaRepository.existsByEmpresaIdAndUsername(
                empresaId.valor().toString(),
                username.valor()
        );
    }

    @Override
    public boolean existePorEmail(EmpresaId empresaId, Email email) {
        return jpaRepository.existsByEmpresaIdAndEmail(
                empresaId.valor().toString(),
                email.valor()
        );
    }

    @Override
    public List<Usuario> buscarPorEmpresa(EmpresaId empresaId) {
        List<UsuarioJpaEntity> entities = jpaRepository.findByEmpresaId(empresaId.valor().toString());
        return UsuarioPersistenceMapper.toDomainEntityList(entities);
    }
}
