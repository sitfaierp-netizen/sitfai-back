package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object inmutable: Identificador único global de un evento almacenado en el Event Store (AUD-03).
 * <p>
 * Representa una clave UUID inmutable y tipada para la trazabilidad y auditoría de eventos.
 */
public record StoredEventId(UUID valor) {

    public StoredEventId {
        Objects.requireNonNull(valor, "StoredEventId: el UUID no puede ser null.");
    }

    public static StoredEventId generar() {
        return new StoredEventId(UUID.randomUUID());
    }

    public static StoredEventId de(UUID valor) {
        return new StoredEventId(valor);
    }

    public static StoredEventId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("StoredEventId: la representación String no puede ser null ni vacía.");
        }
        return new StoredEventId(UUID.fromString(valor.trim()));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
