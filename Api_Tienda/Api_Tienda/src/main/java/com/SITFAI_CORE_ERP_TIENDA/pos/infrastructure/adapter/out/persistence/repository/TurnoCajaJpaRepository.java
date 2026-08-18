package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity.TurnoCajaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TurnoCajaJpaRepository extends JpaRepository<TurnoCajaJpaEntity, String> {
    Optional<TurnoCajaJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
}
