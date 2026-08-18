package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ClienteId(UUID value) {
    public ClienteId {
        Objects.requireNonNull(value, "El ID del cliente no puede ser nulo.");
    }

    public static ClienteId generar() {
        return new ClienteId(UUID.randomUUID());
    }

    public static ClienteId de(String id) {
        return new ClienteId(UUID.fromString(id));
    }

    public static ClienteId de(UUID id) {
        return new ClienteId(id);
    }
}
