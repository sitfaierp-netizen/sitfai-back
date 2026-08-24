package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model;

/**
 * Value Object que representa la huella (hash SHA-256) del payload del request original.
 */
public record PayloadFingerprint(String value) {
    public PayloadFingerprint {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El fingerprint del payload no puede ser nulo o vacío.");
        }
    }
}
