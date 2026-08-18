package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.util.UUID;

public record ProductoId(UUID valor) {
    public ProductoId {
        if (valor == null) {
            throw new IllegalArgumentException("El ProductoId no puede ser nulo");
        }
    }
}
