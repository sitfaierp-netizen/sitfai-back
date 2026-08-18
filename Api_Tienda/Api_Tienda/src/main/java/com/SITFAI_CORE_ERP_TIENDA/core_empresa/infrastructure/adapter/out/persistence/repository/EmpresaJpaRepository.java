package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaJpaRepository extends JpaRepository<EmpresaJpaEntity, String> {
    boolean existsByRuc(String ruc);
    Optional<EmpresaJpaEntity> findByRuc(String ruc);
}
