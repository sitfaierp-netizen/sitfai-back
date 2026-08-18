package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.util.UUID;

public record ProveedorId(UUID valor) {
    public ProveedorId {
        if (valor == null) {
            throw new IllegalArgumentException("El ProveedorId no puede ser nulo");
        }
    }
}
