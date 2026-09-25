package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo;

import java.util.UUID;

public record EmpresaId(UUID valor) {
    public EmpresaId {
        if (valor == null) throw new IllegalArgumentException("El id de la empresa no puede ser nulo (Regla MT-01)");
    }
    public static EmpresaId de(UUID id) {
        return new EmpresaId(id);
    }
}
