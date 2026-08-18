package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.persistence.entity.ResolucionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResolucionJpaRepository extends JpaRepository<ResolucionJpaEntity, String> {
    
    Optional<ResolucionJpaEntity> findByEmpresaIdAndActivaTrue(String empresaId);
    
}
