package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo;

import java.util.UUID;

public record InsumoId(UUID valor) {
    public InsumoId {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de InsumoId no puede ser nulo");
        }
    }
}
