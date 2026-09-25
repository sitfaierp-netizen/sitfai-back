package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo;

import java.util.Objects;
import java.util.UUID;

public record DespachoId(UUID valor) {
    public DespachoId {
        Objects.requireNonNull(valor, "El valor de DespachoId no puede ser nulo");
    }

    public static DespachoId de(UUID id) {
        return new DespachoId(id);
    }

    public static DespachoId generar() {
        return new DespachoId(UUID.randomUUID());
    }
}
