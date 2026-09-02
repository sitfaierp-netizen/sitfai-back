package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public record ProveedorId(UUID valor) {
    public ProveedorId {
        Objects.requireNonNull(valor, "El valor de ProveedorId no puede ser nulo.");
    }

    public static ProveedorId de(String id) {
        return new ProveedorId(UUID.fromString(id));
    }
}
