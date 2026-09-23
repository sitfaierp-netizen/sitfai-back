package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.DespachoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DespachoJpaRepository extends JpaRepository<DespachoJpaEntity, UUID> {

    Optional<DespachoJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);

    Optional<DespachoJpaEntity> findByPedidoIdAndEmpresaId(UUID pedidoId, UUID empresaId);

    List<DespachoJpaEntity> findAllByEmpresaId(UUID empresaId);
}
