package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo;

import java.util.UUID;

public record PedidoId(UUID value) {
    public PedidoId {
        if (value == null) {
            throw new IllegalArgumentException("PedidoId no puede ser nulo");
        }
    }
}
