package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence.entity.AutorizacionDevolucionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataAutorizacionDevolucionRepository extends JpaRepository<AutorizacionDevolucionJpaEntity, String> {
    Optional<AutorizacionDevolucionJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
}
