package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo;

import java.util.UUID;

public record ProductoFinalId(UUID valor) {
    public ProductoFinalId {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de ProductoFinalId no puede ser nulo");
        }
    }
}
