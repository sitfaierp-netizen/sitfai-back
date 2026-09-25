package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo;

import java.util.UUID;

public record RecetaId(UUID valor) {
    public RecetaId {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de RecetaId no puede ser nulo");
        }
    }
}
