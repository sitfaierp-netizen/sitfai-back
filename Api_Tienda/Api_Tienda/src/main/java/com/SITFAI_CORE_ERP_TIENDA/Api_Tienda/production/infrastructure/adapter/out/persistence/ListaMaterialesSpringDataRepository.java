package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity.ListaMaterialesJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListaMaterialesSpringDataRepository extends JpaRepository<ListaMaterialesJpaEntity, UUID> {
    Optional<ListaMaterialesJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
