package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.BodegaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.BodegaJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.mapper.BodegaPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de Salida (Driven Adapter): Implementación JPA del puerto {@link BodegaRepository}.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-2).
 * Resuelve la inyección de dependencias para los casos de uso en la Capa de Aplicación,
 * orquestando las llamadas a Spring Data JPA y la transformación bidireccional con el Dominio.
 * <p>
 * Reglas validadas:
 * <ul>
 *   <li>REGLA-1 / REGLA-2: Implementación de Driven Ports en persistence/adapter.</li>
 *   <li>MT-01 / MT-02: Todas las operaciones filtran por {@code empresaId}.</li>
 *   <li>BOD-02: Verificación de unicidad de código por sucursal y empresa.</li>
 * </ul>
 */
@Repository
public class BodegaJpaAdapter implements BodegaRepository {

    private final BodegaJpaRepository bodegaJpaRepository;
    private final BodegaPersistenceMapper mapper;

    public BodegaJpaAdapter(BodegaJpaRepository bodegaJpaRepository, BodegaPersistenceMapper mapper) {
        this.bodegaJpaRepository = Objects.requireNonNull(bodegaJpaRepository, "bodegaJpaRepository no puede ser null");
        this.mapper = Objects.requireNonNull(mapper, "mapper no puede ser null");
    }

    @Override
    public Bodega guardar(Bodega bodega) {
        Objects.requireNonNull(bodega, "Bodega a guardar no puede ser null");
        BodegaJpaEntity jpaEntity = mapper.toJpaEntity(bodega);
        BodegaJpaEntity savedEntity = bodegaJpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Bodega> buscarPorId(BodegaId bodegaId, EmpresaId empresaId) {
        Objects.requireNonNull(bodegaId, "bodegaId no puede ser null");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null (MT-01)");
        return bodegaJpaRepository.findByIdAndEmpresaId(bodegaId.valor(), empresaId.valor())
                .map(mapper::toDomain);
    }

    @Override
    public List<Bodega> listarActivasPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser null (MT-01)");
        return bodegaJpaRepository.findByEmpresaIdAndActivaTrue(empresaId.valor())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existeCodigoEnSucursal(EmpresaId empresaId, String sucursalId, String codigoBodega) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser null (MT-01)");
        Objects.requireNonNull(sucursalId, "sucursalId no puede ser null");
        Objects.requireNonNull(codigoBodega, "codigoBodega no puede ser null");

        UUID sucursalUuid = UUID.fromString(sucursalId);
        return bodegaJpaRepository.existsByEmpresaIdAndSucursalIdAndCodigo(
                empresaId.valor(),
                sucursalUuid,
                codigoBodega.trim().toUpperCase()
        );
    }
}
