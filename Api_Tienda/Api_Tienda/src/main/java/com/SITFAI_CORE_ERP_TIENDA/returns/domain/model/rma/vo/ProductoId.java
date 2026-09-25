package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo;

import java.util.UUID;

public record ProductoId(UUID valor) {
    public ProductoId {
        if (valor == null) throw new IllegalArgumentException("El id del producto no puede ser nulo");
    }
    public static ProductoId de(UUID id) {
        return new ProductoId(id);
    }
}
