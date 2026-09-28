package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo;

import java.util.Objects;
import java.util.UUID;

public record PedidoId(UUID valor) {
    public PedidoId {
        Objects.requireNonNull(valor, "El valor de PedidoId no puede ser nulo");
    }

    public static PedidoId de(UUID id) {
        return new PedidoId(id);
    }
}
