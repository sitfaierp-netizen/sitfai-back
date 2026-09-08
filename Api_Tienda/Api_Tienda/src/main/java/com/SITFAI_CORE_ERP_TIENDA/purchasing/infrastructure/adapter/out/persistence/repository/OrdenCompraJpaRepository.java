package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.OrdenCompraJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdenCompraJpaRepository extends JpaRepository<OrdenCompraJpaEntity, String> {
    Optional<OrdenCompraJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
    org.springframework.data.domain.Page<OrdenCompraJpaEntity> findByEmpresaId(String empresaId, org.springframework.data.domain.Pageable pageable);
}
