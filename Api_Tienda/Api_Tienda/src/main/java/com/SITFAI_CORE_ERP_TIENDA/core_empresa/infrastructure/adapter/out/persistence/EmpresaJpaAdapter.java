package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Empresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmpresaJpaAdapter implements EmpresaRepository {

    private final EmpresaJpaRepository repository;

    public EmpresaJpaAdapter(EmpresaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existePorRuc(Ruc ruc) {
        return repository.existsByRuc(ruc.valor());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Empresa guardar(Empresa empresa) {
        EmpresaJpaEntity entity = toEntity(empresa);
        try {
            EmpresaJpaEntity guardada = repository.saveAndFlush(entity);
            return toDomain(guardada);
        } catch (RuntimeException e) {
            if (esErrorDeConcurrencia(e)) {
                throw new com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.OptimisticConcurrencyException("Empresa", empresa.getId().valor());
            }
            throw e;
        }
    }

    private boolean esErrorDeConcurrencia(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof org.springframework.dao.OptimisticLockingFailureException ||
                cause instanceof jakarta.persistence.OptimisticLockException ||
                cause instanceof org.hibernate.StaleObjectStateException ||
                cause instanceof org.springframework.orm.jpa.JpaSystemException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<Empresa> buscarPorId(EmpresaId id) {
        return repository.findById(id.valor().toString())
                .map(this::toDomain);
    }

    @Override
    public boolean existe(EmpresaId id) {
        return repository.existsById(id.valor().toString());
    }


    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Optional<Empresa> buscarPorRuc(Ruc ruc) {
        return repository.findByRuc(ruc.valor()).map(this::toDomain);
    }

    @Override
    public java.util.List<Empresa> buscarTodas() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private EmpresaJpaEntity toEntity(Empresa domain) {
        EmpresaJpaEntity entity = new EmpresaJpaEntity();
        entity.setId(domain.getId().valor().toString());
        entity.setRuc(domain.getRuc().valor());
        entity.setRazonSocial(domain.getNombre().valor());
        entity.setEstado(domain.getEstado().name());
        entity.setCreadoEn(domain.getCreadoEn());
        entity.setActualizadoEn(domain.getActualizadoEn());
        entity.setVersion(domain.getVersion() != null ? domain.getVersion() : 0L);
        return entity;
    }

    private Empresa toDomain(EmpresaJpaEntity entity) {
        return Empresa.reconstituir(
                new EmpresaId(UUID.fromString(entity.getId())),
                new Ruc(entity.getRuc()),
                new NombreEmpresa(entity.getRazonSocial()),
                EstadoEmpresa.valueOf(entity.getEstado()),
                Collections.emptyList(), // En un caso real recuperaríamos sucursales si fuera necesario o se modela como aggregate separado
                entity.getCreadoEn(),
                entity.getActualizadoEn(),
                entity.getVersion()
        );
    }
}
