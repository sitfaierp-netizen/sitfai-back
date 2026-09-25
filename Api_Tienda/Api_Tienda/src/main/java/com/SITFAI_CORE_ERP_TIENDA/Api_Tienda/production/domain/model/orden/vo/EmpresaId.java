package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo;

import java.util.UUID;

public record EmpresaId(UUID valor) {
    public EmpresaId {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de EmpresaId no puede ser nulo");
        }
    }
}
