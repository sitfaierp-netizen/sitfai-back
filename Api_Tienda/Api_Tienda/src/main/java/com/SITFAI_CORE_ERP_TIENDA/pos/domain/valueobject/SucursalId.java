package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record SucursalId(UUID value) {
    public SucursalId {
        Objects.requireNonNull(value, "SucursalId no puede ser nulo");
    }
}
