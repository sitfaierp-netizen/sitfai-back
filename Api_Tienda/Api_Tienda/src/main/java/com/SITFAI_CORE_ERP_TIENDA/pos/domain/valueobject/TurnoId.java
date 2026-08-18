package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TurnoId(UUID value) {
    public TurnoId {
        Objects.requireNonNull(value, "TurnoId no puede ser nulo");
    }

    public static TurnoId generar() {
        return new TurnoId(UUID.randomUUID());
    }
}
