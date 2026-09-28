package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo;

import java.util.Objects;
import java.util.UUID;

public record EmpresaId(UUID valor) {
    public EmpresaId {
        Objects.requireNonNull(valor, "El valor de EmpresaId no puede ser nulo");
    }

    public static EmpresaId de(UUID id) {
        return new EmpresaId(id);
    }
}
