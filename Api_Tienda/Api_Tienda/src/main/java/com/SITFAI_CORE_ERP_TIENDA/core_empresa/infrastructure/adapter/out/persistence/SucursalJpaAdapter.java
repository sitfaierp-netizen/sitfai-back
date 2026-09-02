package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoSucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.SucursalJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository.SucursalJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida (Driven Adapter): implementa el puerto {@link SucursalRepository}
 * usando Spring Data JPA. Garantiza el aislamiento multitenant (MT-02).
 */
@Component
public class SucursalJpaAdapter implements SucursalRepository {

    private final SucursalJpaRepository sucursalJpaRepository;
    private final EmpresaJpaRepository empresaJpaRepository;

    public SucursalJpaAdapter(
            SucursalJpaRepository sucursalJpaRepository,
            EmpresaJpaRepository empresaJpaRepository) {
        this.sucursalJpaRepository = sucursalJpaRepository;
        this.empresaJpaRepository = empresaJpaRepository;
    }

    @Override
    public List<Sucursal> buscarPorEmpresaId(EmpresaId empresaId) {
        return sucursalJpaRepository
                .findAllByEmpresa_Id(empresaId.valor().toString())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Sucursal> buscarPorIdYEmpresaId(SucursalId sucursalId, EmpresaId empresaId) {
        return sucursalJpaRepository
                .findByIdAndEmpresa_Id(sucursalId.valor().toString(), empresaId.valor().toString())
                .map(this::toDomain);
    }

    @Override
    public Sucursal guardar(Sucursal sucursal, EmpresaId empresaId) {
        // Recuperar la referencia JPA de la Empresa para mantener la FK
        EmpresaJpaEntity empresaRef = empresaJpaRepository.getReferenceById(empresaId.valor().toString());

        SucursalJpaEntity entity = toJpaEntity(sucursal, empresaRef);
        SucursalJpaEntity guardada = sucursalJpaRepository.save(entity);
        return toDomain(guardada);
    }

    @Override
    public boolean existePorEmpresaIdYCodigo(EmpresaId empresaId, String codigo) {
        return sucursalJpaRepository.existsByEmpresa_IdAndCodigo(empresaId.valor().toString(), codigo);
    }

    // --- Mappers internos (dominio ↔ JPA) ---

    private Sucursal toDomain(SucursalJpaEntity entity) {
        return Sucursal.reconstituir(
                SucursalId.de(entity.getId()),
                entity.getCodigo(),
                entity.getNombre(),
                EstadoSucursal.valueOf(entity.getEstado()),
                entity.getCreadoEn(),
                entity.getActualizadoEn(),
                entity.isActivo(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    private SucursalJpaEntity toJpaEntity(Sucursal domain, EmpresaJpaEntity empresaRef) {
        SucursalJpaEntity entity = new SucursalJpaEntity();
        entity.setId(domain.getId().valor().toString());
        entity.setEmpresa(empresaRef);
        entity.setCodigo(domain.getCodigo());
        entity.setNombre(domain.getNombre());
        entity.setEstado(domain.getEstado().name());
        entity.setCreadoEn(domain.getCreadoEn());
        entity.setActualizadoEn(domain.getActualizadoEn());
        return entity;
    }
}
