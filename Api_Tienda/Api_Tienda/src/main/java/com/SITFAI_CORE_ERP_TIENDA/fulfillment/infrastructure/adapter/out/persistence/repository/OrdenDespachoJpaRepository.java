package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity.OrdenDespachoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdenDespachoJpaRepository extends JpaRepository<OrdenDespachoJpaEntity, String> {
    Optional<OrdenDespachoJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
}
