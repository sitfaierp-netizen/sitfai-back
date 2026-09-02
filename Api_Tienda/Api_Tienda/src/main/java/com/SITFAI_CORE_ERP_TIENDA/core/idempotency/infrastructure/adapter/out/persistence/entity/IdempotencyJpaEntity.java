package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "core_idempotency_record", uniqueConstraints = {
        @UniqueConstraint(name = "uq_core_idempotency_empresa_key", columnNames = {"empresa_id", "idempotency_key"})
})
public class IdempotencyJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, length = 36)
    private String empresaId;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IdempotencyJpaEntity() {}

    public IdempotencyJpaEntity(UUID id, String empresaId, String idempotencyKey, String requestHash,
                                String status, Integer httpStatus, String responseBody, Instant createdAt) {
        this.id = id;
        this.empresaId = empresaId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = status;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEmpresaId() { return empresaId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public String getStatus() { return status; }
    public Integer getHttpStatus() { return httpStatus; }
    public String getResponseBody() { return responseBody; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(String status) { this.status = status; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
}
