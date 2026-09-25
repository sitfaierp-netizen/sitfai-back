package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo;

import java.util.UUID;

public record BodegaId(UUID valor) {
    public BodegaId {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de BodegaId no puede ser nulo");
        }
    }
}
