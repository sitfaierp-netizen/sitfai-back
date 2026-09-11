package com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo;

import java.util.UUID;

public record PedidoId(UUID value) {
    public PedidoId {
        if (value == null) {
            throw new IllegalArgumentException("El ID del pedido no puede ser nulo");
        }
    }
}
