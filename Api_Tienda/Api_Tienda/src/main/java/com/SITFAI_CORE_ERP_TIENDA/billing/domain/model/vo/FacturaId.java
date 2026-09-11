package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo;

import java.util.UUID;

public record FacturaId(UUID value) {
    public FacturaId {
        if (value == null) {
            throw new IllegalArgumentException("FacturaId no puede ser nulo");
        }
    }
}
