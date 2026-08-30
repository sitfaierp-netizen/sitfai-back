package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, UUID> {
    Optional<PedidoJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
