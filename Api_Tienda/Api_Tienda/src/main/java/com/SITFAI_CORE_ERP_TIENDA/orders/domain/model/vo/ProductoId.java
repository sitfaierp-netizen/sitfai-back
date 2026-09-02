package com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo;

import java.util.UUID;

public record ProductoId(UUID value) {
    public ProductoId {
        if (value == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo");
        }
    }
}
