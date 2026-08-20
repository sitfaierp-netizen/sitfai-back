package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.BodegaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link BodegaJpaEntity}.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-6).
 * Incluye consultas filtradas por {@code empresa_id} para garantizar aislamiento multitenant (MT-01, MT-02).
 */
@Repository
public interface BodegaJpaRepository extends JpaRepository<BodegaJpaEntity, UUID> {

    /**
     * Busca una Bodega por su ID y su tenant propietario.
     */
    Optional<BodegaJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);

    /**
     * Lista todas las Bodegas activas de un tenant.
     */
    List<BodegaJpaEntity> findByEmpresaIdAndActivaTrue(UUID empresaId);

    /**
     * Verifica si ya existe una Bodega con el mismo código dentro de una Sucursal para un tenant (BOD-02).
     */
    boolean existsByEmpresaIdAndSucursalIdAndCodigo(UUID empresaId, UUID sucursalId, String codigo);

    /**
     * Lista todas las Bodegas de una Sucursal específica (MT-01).
     */
    List<BodegaJpaEntity> findByEmpresaIdAndSucursalId(UUID empresaId, UUID sucursalId);

    /**
     * Lista todas las Bodegas por Sucursal Id.
     */
    List<BodegaJpaEntity> findBySucursalId(UUID sucursalId);
}
