package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo;

import java.util.Objects;
import java.util.UUID;

public record BodegaId(UUID valor) {
    public BodegaId {
        Objects.requireNonNull(valor, "El valor de BodegaId no puede ser nulo");
    }

    public static BodegaId de(UUID id) {
        return new BodegaId(id);
    }
}
