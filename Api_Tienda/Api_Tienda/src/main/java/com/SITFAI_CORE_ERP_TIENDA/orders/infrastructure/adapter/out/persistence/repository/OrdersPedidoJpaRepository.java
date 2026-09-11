package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity.OrdersPedidoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrdersPedidoJpaRepository extends JpaRepository<OrdersPedidoJpaEntity, UUID> {
    Optional<OrdersPedidoJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
