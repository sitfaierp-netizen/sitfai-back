package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ProductoId(UUID value) {
    public ProductoId {
        Objects.requireNonNull(value, "ProductoId no puede ser nulo");
    }
}
