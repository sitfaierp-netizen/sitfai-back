package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo;

import java.util.UUID;

public record OrdenProduccionId(UUID valor) {
    public OrdenProduccionId {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de OrdenProduccionId no puede ser nulo");
        }
    }
}
