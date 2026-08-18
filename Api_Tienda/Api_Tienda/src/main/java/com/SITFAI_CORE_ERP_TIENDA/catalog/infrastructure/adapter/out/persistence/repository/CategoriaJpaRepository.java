package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity.CategoriaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Spring Data Repository para CategoriaJpaEntity.
 */
public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, String> {
    Optional<CategoriaJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
}
