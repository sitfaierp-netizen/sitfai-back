package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record PedidoOrigenId(UUID value) {
    public PedidoOrigenId {
        Objects.requireNonNull(value, "PedidoOrigenId no puede ser nulo");
    }
}
