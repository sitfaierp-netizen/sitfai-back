package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity.ProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data Repository para ProductoJpaEntity.
 * Aislamiento estricto de MT-01 en todas las consultas.
 */
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, String> {
    Optional<ProductoJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
    List<ProductoJpaEntity> findAllByEmpresaId(String empresaId);
    boolean existsByCodigoBarrasAndEmpresaIdAndIdNot(String codigoBarras, String empresaId, String id);
}
