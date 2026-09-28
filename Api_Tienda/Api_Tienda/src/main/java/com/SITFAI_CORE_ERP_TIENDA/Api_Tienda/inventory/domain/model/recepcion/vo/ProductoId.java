package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo;

import java.util.UUID;

public record ProductoId(UUID valor) {
    public ProductoId {
        if (valor == null) {
            throw new IllegalArgumentException("ProductoId: el UUID no puede ser null.");
        }
    }
}
