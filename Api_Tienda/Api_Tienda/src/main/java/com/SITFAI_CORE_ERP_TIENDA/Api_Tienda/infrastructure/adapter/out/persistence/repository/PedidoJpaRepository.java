package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, String> {
    
    // MT-01: Siempre buscar usando empresaId
    Optional<PedidoJpaEntity> findByIdAndEmpresaId(String id, String empresaId);

    List<PedidoJpaEntity> findByEmpresaId(String empresaId);

    boolean existsByIdAndEmpresaId(String id, String empresaId);
}
