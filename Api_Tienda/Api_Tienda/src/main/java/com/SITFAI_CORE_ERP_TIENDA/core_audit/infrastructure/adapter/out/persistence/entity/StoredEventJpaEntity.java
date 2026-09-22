package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA: Representa el registro persistente de un Evento de Dominio en la tabla {@code core_audit_event_store}.
 * <p>
 * Regla AUD-03: Almacenamiento inmutable y permanente de eventos.
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Trazabilidad de creación heredando de {@link AuditableJpaEntity}.
 */
@Entity
@Table(name = "core_audit_event_store", uniqueConstraints = {
        @UniqueConstraint(name = "uq_core_audit_event_store_empresa_id", columnNames = {"empresa_id", "id"})
})
public class StoredEventJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "empresa_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID empresaId;

    @Column(name = "nombre_evento", updatable = false, nullable = false, length = 150)
    private String nombreEvento;

    @Column(name = "ocurrido_en", updatable = false, nullable = false)
    private Instant ocurridoEn;

    @Lob
    @Column(name = "payload", updatable = false, nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EventStatus estado;

    @Column(name = "motivo_fallo", length = 500)
    private String motivoFallo;

    @Column(name = "procesado_en")
    private Instant procesadoEn;

    public StoredEventJpaEntity() {
    }

    public StoredEventJpaEntity(
            UUID id,
            UUID empresaId,
            String nombreEvento,
            Instant ocurridoEn,
            String payload,
            EventStatus estado,
            String motivoFallo,
            Instant procesadoEn
    ) {
        this.id = id;
        this.empresaId = empresaId;
        this.nombreEvento = nombreEvento;
        this.ocurridoEn = ocurridoEn;
        this.payload = payload;
        this.estado = estado;
        this.motivoFallo = motivoFallo;
        this.procesadoEn = procesadoEn;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public void setNombreEvento(String nombreEvento) {
        this.nombreEvento = nombreEvento;
    }

    public Instant getOcurridoEn() {
        return ocurridoEn;
    }

    public void setOcurridoEn(Instant ocurridoEn) {
        this.ocurridoEn = ocurridoEn;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public EventStatus getEstado() {
        return estado;
    }

    public void setEstado(EventStatus estado) {
        this.estado = estado;
    }

    public String getMotivoFallo() {
        return motivoFallo;
    }

    public void setMotivoFallo(String motivoFallo) {
        this.motivoFallo = motivoFallo;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }

    public void setProcesadoEn(Instant procesadoEn) {
        this.procesadoEn = procesadoEn;
    }
}
