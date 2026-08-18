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
    public Empresa guardar(Empresa empresa) {
        EmpresaJpaEntity entity = toEntity(empresa);
        EmpresaJpaEntity guardada = repository.save(entity);
        return toDomain(guardada);
    }

    @Override
    public Optional<Empresa> buscarPorId(EmpresaId id) {
        return repository.findById(id.valor().toString())
                .map(this::toDomain);
    }

    @Override
    public boolean existe(EmpresaId id) {
        return repository.existsById(id.valor().toString());
    }

    @Override
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
                entity.getActualizadoEn()
        );
    }
}
