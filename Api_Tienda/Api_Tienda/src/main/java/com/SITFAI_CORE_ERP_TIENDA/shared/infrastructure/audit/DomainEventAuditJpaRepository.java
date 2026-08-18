package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la persistencia del Event Store (AUD-03, MT-01).
 */
@Repository
public interface DomainEventAuditJpaRepository extends JpaRepository<DomainEventAuditJpaEntity, String> {

    List<DomainEventAuditJpaEntity> findByEmpresaIdOrderByOccurredOnDesc(String empresaId);

    List<DomainEventAuditJpaEntity> findByAggregateIdOrderByOccurredOnAsc(String aggregateId);

    List<DomainEventAuditJpaEntity> findByEmpresaIdAndEventTypeOrderByOccurredOnDesc(String empresaId, String eventType);
}
