package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.util.UUID;

public record EmpresaId(UUID valor) {
    public EmpresaId {
        if (valor == null) {
            throw new IllegalArgumentException("El EmpresaId no puede ser nulo (MT-01)");
        }
    }
}
