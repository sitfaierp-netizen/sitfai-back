package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo;

import java.util.Objects;
import java.util.UUID;

public record ProductoId(UUID valor) {
    public ProductoId {
        Objects.requireNonNull(valor, "El valor de ProductoId no puede ser nulo");
    }

    public static ProductoId de(UUID id) {
        return new ProductoId(id);
    }
}
