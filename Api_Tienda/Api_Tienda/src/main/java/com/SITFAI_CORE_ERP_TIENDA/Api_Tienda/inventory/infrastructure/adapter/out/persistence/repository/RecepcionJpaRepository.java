package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.RecepcionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecepcionJpaRepository extends JpaRepository<RecepcionJpaEntity, UUID> {
    
    Optional<RecepcionJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
