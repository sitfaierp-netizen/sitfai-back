package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.entity.IdempotencyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyJpaEntity, UUID> {
    Optional<IdempotencyJpaEntity> findByEmpresaIdAndIdempotencyKey(String empresaId, String idempotencyKey);
}
