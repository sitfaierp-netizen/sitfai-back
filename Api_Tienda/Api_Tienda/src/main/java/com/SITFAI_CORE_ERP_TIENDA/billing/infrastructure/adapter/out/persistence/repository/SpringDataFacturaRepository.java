package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.FacturaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataFacturaRepository extends JpaRepository<FacturaJpaEntity, UUID> {
    Optional<FacturaJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
    List<FacturaJpaEntity> findByEmpresaId(UUID empresaId);
    boolean existsByIdAndEmpresaId(UUID id, UUID empresaId);
}
