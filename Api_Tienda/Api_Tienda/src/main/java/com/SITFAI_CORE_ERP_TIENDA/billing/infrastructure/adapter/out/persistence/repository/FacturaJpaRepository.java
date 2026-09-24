package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.FacturaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface FacturaJpaRepository extends JpaRepository<FacturaJpaEntity, UUID> {

    // Regla MT-01: Búsqueda siempre con empresaId
    Optional<FacturaJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
    boolean existsByIdAndEmpresaId(UUID id, UUID empresaId);
}
