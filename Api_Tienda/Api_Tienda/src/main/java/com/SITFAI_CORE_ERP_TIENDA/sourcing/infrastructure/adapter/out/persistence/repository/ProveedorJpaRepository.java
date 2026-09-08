package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.entity.ProveedorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProveedorJpaRepository extends JpaRepository<ProveedorJpaEntity, String> {
    Optional<ProveedorJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
    boolean existsByEmpresaIdAndRuc(String empresaId, String ruc);
    java.util.List<ProveedorJpaEntity> findByEmpresaId(String empresaId);
}
