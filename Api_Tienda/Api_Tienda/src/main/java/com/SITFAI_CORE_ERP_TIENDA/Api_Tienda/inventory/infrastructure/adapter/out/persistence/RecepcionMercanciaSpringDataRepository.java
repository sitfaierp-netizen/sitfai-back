package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.RecepcionMercanciaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecepcionMercanciaSpringDataRepository extends JpaRepository<RecepcionMercanciaJpaEntity, UUID> {
    Optional<RecepcionMercanciaJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
