package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model;

import java.util.Objects;

/**
 * Value Object que representa la llave de idempotencia.
 * Generalmente un UUID.
 */
public record IdempotencyKey(String value) {
    public IdempotencyKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La llave de idempotencia no puede ser nula o vacía.");
        }
        if (value.length() > 64) {
            throw new IllegalArgumentException("La llave de idempotencia excede la longitud máxima (64 caracteres).");
        }
    }
}
