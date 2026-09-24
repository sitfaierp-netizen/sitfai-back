package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo;

import java.util.Objects;
import java.util.UUID;

public record EmpresaId(UUID valor) {
    public EmpresaId {
        Objects.requireNonNull(valor, "El valor de EmpresaId no puede ser nulo (MT-01)");
    }
}
