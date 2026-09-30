package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la persistencia del Event Store (AUD-03, MT-01).
 */
@Repository
public interface DomainEventAuditJpaRepository extends JpaRepository<DomainEventAuditJpaEntity, String> {

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO audit_domain_events
                (id, aggregate_id, event_type, payload, empresa_id, occurred_on)
            VALUES
                (:id, :aggregateId, :eventType, :payload, :empresaId, :occurredOn)
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("id") String id,
            @Param("aggregateId") String aggregateId,
            @Param("eventType") String eventType,
            @Param("payload") String payload,
            @Param("empresaId") String empresaId,
            @Param("occurredOn") Instant occurredOn
    );

    List<DomainEventAuditJpaEntity> findByEmpresaIdOrderByOccurredOnDesc(String empresaId);

    List<DomainEventAuditJpaEntity> findByAggregateIdOrderByOccurredOnAsc(String aggregateId);

    List<DomainEventAuditJpaEntity> findByEmpresaIdAndEventTypeOrderByOccurredOnDesc(String empresaId, String eventType);
}
