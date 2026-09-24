package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.ClasificacionProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository para {@link ClasificacionProductoJpaEntity}.
 * Regla MT-01: Métodos siempre filtrados por {@code empresaId}.
 */
@Repository
public interface SpringDataClasificacionProductoRepository extends JpaRepository<ClasificacionProductoJpaEntity, String> {

    Optional<ClasificacionProductoJpaEntity> findByEmpresaIdAndBodegaIdAndProductoId(
            String empresaId, String bodegaId, String productoId);

    Optional<ClasificacionProductoJpaEntity> findByIdAndEmpresaId(String id, String empresaId);

    List<ClasificacionProductoJpaEntity> findByEmpresaIdAndBodegaId(String empresaId, String bodegaId);
}
