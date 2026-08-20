package com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.valueobject;

import java.time.Instant;

public record AuditData(
    Instant createdAt,
    String createdBy,
    Instant updatedAt,
    String updatedBy
) {
    public AuditData {
        if (createdAt == null) throw new IllegalArgumentException("createdAt cannot be null");
        if (createdBy == null || createdBy.isBlank()) throw new IllegalArgumentException("createdBy cannot be null or blank");
    }

    public static AuditData registrar(String actorId) {
        return new AuditData(Instant.now(), actorId, null, null);
    }

    public AuditData actualizar(String actorId) {
        return new AuditData(this.createdAt, this.createdBy, Instant.now(), actorId);
    }
}
