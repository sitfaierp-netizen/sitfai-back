package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.exception.ConcurrentProcessingException;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.exception.IdempotencyConflictException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: Representa el registro de una llave de idempotencia.
 */
public class IdempotencyRecord {

    private final UUID id;
    private final EmpresaId empresaId;
    private final IdempotencyKey idempotencyKey;
    private final PayloadFingerprint requestHash;
    private IdempotencyStatus status;
    private Integer httpStatus;
    private String responseBody;
    private final Instant createdAt;

    private IdempotencyRecord(UUID id, EmpresaId empresaId, IdempotencyKey idempotencyKey,
                              PayloadFingerprint requestHash, IdempotencyStatus status,
                              Integer httpStatus, String responseBody, Instant createdAt) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "El empresaId es obligatorio.");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "La llave de idempotencia es obligatoria.");
        this.requestHash = Objects.requireNonNull(requestHash, "El hash del request es obligatorio.");
        this.status = Objects.requireNonNull(status, "El estado es obligatorio.");
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria.");
    }

    /**
     * Inicia un nuevo registro en estado PROCESSING.
     */
    public static IdempotencyRecord beginProcessing(EmpresaId empresaId, IdempotencyKey idempotencyKey, PayloadFingerprint requestHash) {
        return new IdempotencyRecord(
                UUID.randomUUID(),
                empresaId,
                idempotencyKey,
                requestHash,
                IdempotencyStatus.PROCESSING,
                null,
                null,
                Instant.now()
        );
    }

    /**
     * Reconstruye el agregado desde persistencia.
     */
    public static IdempotencyRecord reconstitute(UUID id, EmpresaId empresaId, IdempotencyKey idempotencyKey,
                                                 PayloadFingerprint requestHash, IdempotencyStatus status,
                                                 Integer httpStatus, String responseBody, Instant createdAt) {
        return new IdempotencyRecord(id, empresaId, idempotencyKey, requestHash, status, httpStatus, responseBody, createdAt);
    }

    /**
     * Transita el estado a COMPLETADO.
     */
    public void markAsCompleted(Integer httpStatus, String responseBody) {
        if (this.status != IdempotencyStatus.PROCESSING && this.status != IdempotencyStatus.FAILED) {
            throw new IllegalStateException("Solo se puede completar una operación en progreso o fallida.");
        }
        this.status = IdempotencyStatus.COMPLETED;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    /**
     * Transita el estado a FAILED.
     */
    public void markAsFailed(Integer httpStatus, String responseBody) {
        if (this.status != IdempotencyStatus.PROCESSING) {
            throw new IllegalStateException("Solo se puede marcar como fallida una operación en progreso.");
        }
        this.status = IdempotencyStatus.FAILED;
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    /**
     * Valida si se puede reprocesar y maneja concurrencia/conflictos.
     */
    public void checkAndLockForProcessing(PayloadFingerprint currentRequestHash) {
        if (!this.requestHash.equals(currentRequestHash)) {
            throw new IdempotencyConflictException("La llave de idempotencia ya fue usada con un payload distinto.");
        }

        if (this.status == IdempotencyStatus.PROCESSING) {
            throw new ConcurrentProcessingException("La operación se está procesando actualmente. Intente de nuevo más tarde.");
        }

        if (this.status == IdempotencyStatus.COMPLETED) {
            // Ya completado, el flujo HTTP debería simplemente devolver la respuesta cacheada.
            // No cambiamos el estado aquí.
            return;
        }

        if (this.status == IdempotencyStatus.FAILED || this.status == IdempotencyStatus.EXPIRED) {
            // Permitir reproceso
            this.status = IdempotencyStatus.PROCESSING;
        }
    }

    public boolean isCompleted() {
        return this.status == IdempotencyStatus.COMPLETED;
    }

    /**
     * Lógica para expirar por TTL.
     */
    public void checkExpiration(Instant now, long ttlMillis) {
        if (this.status == IdempotencyStatus.PROCESSING && now.toEpochMilli() - this.createdAt.toEpochMilli() > ttlMillis) {
            this.status = IdempotencyStatus.EXPIRED;
        }
    }

    public UUID getId() { return id; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public IdempotencyKey getIdempotencyKey() { return idempotencyKey; }
    public PayloadFingerprint getRequestHash() { return requestHash; }
    public IdempotencyStatus getStatus() { return status; }
    public Integer getHttpStatus() { return httpStatus; }
    public String getResponseBody() { return responseBody; }
    public Instant getCreatedAt() { return createdAt; }
}
