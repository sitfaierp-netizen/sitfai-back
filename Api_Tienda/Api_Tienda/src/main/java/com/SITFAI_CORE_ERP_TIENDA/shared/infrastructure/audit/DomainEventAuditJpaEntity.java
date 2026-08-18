package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidad JPA para el almacenamiento inmutable de Eventos de Dominio (AUD-03, MT-01).
 * Representa el Event Store / Registro de Auditoría global del ERP.
 */
@Entity
@Table(name = "audit_domain_events")
public class DomainEventAuditJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "aggregate_id", length = 36, updatable = false)
    private String aggregateId;

    @Column(name = "event_type", length = 150, nullable = false, updatable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String payload;

    @Column(name = "empresa_id", length = 36, updatable = false)
    private String empresaId;

    @Column(name = "occurred_on", nullable = false, updatable = false)
    private Instant occurredOn;

    public DomainEventAuditJpaEntity() {
    }

    public DomainEventAuditJpaEntity(
            String id,
            String aggregateId,
            String eventType,
            String payload,
            String empresaId,
            Instant occurredOn
    ) {
        this.id = Objects.requireNonNull(id, "id no puede ser null");
        this.aggregateId = aggregateId;
        this.eventType = Objects.requireNonNull(eventType, "eventType no puede ser null");
        this.payload = Objects.requireNonNull(payload, "payload no puede ser null");
        this.empresaId = empresaId;
        this.occurredOn = Objects.requireNonNull(occurredOn, "occurredOn no puede ser null");
    }

    public String getId() {
        return id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEventAuditJpaEntity that = (DomainEventAuditJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DomainEventAuditJpaEntity{" +
                "id='" + id + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", empresaId='" + empresaId + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
