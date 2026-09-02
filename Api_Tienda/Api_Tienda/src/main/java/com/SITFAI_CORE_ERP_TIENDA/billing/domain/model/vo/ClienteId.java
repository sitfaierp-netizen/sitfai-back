package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo;

import java.util.UUID;

public record ClienteId(UUID value) {
    public ClienteId {
        if (value == null) {
            throw new IllegalArgumentException("ClienteId no puede ser nulo");
        }
    }
}
