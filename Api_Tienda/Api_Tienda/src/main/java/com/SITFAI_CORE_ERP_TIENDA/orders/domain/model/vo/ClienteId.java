package com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo;

import java.util.UUID;

public record ClienteId(UUID value) {
    public ClienteId {
        if (value == null) {
            throw new IllegalArgumentException("El ID del cliente no puede ser nulo");
        }
    }
}
