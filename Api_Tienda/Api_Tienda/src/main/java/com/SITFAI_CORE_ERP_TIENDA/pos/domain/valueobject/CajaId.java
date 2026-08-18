package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CajaId(UUID value) {
    public CajaId {
        Objects.requireNonNull(value, "CajaId no puede ser nulo");
    }
}
