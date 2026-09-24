package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.out.persistence.entity.PoliticaInventarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA Repository para PoliticaInventarioJpaEntity.
 * Regla MT-01: Toda consulta DEBE filtrar por empresaId.
 */
public interface SpringDataPoliticaInventarioRepository extends JpaRepository<PoliticaInventarioJpaEntity, String> {

    Optional<PoliticaInventarioJpaEntity> findByIdAndEmpresaId(String id, String empresaId);

    Optional<PoliticaInventarioJpaEntity> findByBodegaIdAndProductoIdAndEmpresaIdAndActivaTrue(
            String bodegaId, String productoId, String empresaId
    );
}
