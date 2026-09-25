package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo;

import java.util.UUID;

public record EmpresaId(UUID valor) {
    public EmpresaId {
        if (valor == null) {
            throw new IllegalArgumentException("EmpresaId: el UUID no puede ser null (Regla MT-01).");
        }
    }
}
