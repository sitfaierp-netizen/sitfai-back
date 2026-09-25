package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo;

import java.util.UUID;

public record BodegaId(UUID valor) {
    public BodegaId {
        if (valor == null) {
            throw new IllegalArgumentException("BodegaId: el UUID no puede ser null.");
        }
    }
}
