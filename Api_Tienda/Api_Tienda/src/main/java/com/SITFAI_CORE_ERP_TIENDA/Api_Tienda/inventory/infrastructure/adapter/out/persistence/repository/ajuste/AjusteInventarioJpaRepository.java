package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.ajuste;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.jpa.entity.ajuste.AjusteInventarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AjusteInventarioJpaRepository extends JpaRepository<AjusteInventarioJpaEntity, UUID> {
    Optional<AjusteInventarioJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
