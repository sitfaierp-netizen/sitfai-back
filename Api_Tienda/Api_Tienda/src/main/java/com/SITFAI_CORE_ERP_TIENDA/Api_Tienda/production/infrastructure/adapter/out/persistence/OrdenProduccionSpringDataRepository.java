package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity.OrdenProduccionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenProduccionSpringDataRepository extends JpaRepository<OrdenProduccionJpaEntity, UUID> {
    Optional<OrdenProduccionJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
