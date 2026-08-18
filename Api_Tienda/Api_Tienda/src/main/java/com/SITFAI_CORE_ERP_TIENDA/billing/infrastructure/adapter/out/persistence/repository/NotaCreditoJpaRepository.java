package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.NotaCreditoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotaCreditoJpaRepository extends JpaRepository<NotaCreditoJpaEntity, String> {

    // Regla MT-01: Aislamiento por Empresa
    Optional<NotaCreditoJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
}
