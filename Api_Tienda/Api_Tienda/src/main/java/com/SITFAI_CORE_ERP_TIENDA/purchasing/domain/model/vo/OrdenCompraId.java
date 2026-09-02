package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record OrdenCompraId(UUID valor) {
    public OrdenCompraId {
        Objects.requireNonNull(valor, "El valor de OrdenCompraId no puede ser nulo.");
    }

    public static OrdenCompraId generar() {
        return new OrdenCompraId(UUID.randomUUID());
    }

    public static OrdenCompraId de(String id) {
        return new OrdenCompraId(UUID.fromString(id));
    }
}
