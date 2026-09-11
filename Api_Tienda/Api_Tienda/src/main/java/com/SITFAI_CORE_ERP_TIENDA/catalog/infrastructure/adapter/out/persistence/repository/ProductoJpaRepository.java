package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity.ProductoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

/**
 * Spring Data Repository para ProductoJpaEntity.
 * Aislamiento estricto de MT-01 en todas las consultas.
 */
public interface ProductoJpaRepository extends JpaRepository<ProductoJpaEntity, String> {
    Optional<ProductoJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
    Page<ProductoJpaEntity> findAllByEmpresaId(String empresaId, Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProductoJpaEntity p WHERE p.empresaId = :empresaId AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProductoJpaEntity> searchByEmpresaIdAndKeyword(@org.springframework.data.repository.query.Param("empresaId") String empresaId, @org.springframework.data.repository.query.Param("search") String search, Pageable pageable);
    
    boolean existsByCodigoBarrasAndEmpresaIdAndIdNot(String codigoBarras, String empresaId, String id);
}
