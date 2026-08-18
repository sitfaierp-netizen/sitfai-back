package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record DespachoId(UUID value) {
    public DespachoId {
        Objects.requireNonNull(value, "DespachoId no puede ser nulo");
    }

    public static DespachoId generar() {
        return new DespachoId(UUID.randomUUID());
    }
}
