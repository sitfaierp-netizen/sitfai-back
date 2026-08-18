package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record EmpresaId(UUID value) {
    public EmpresaId {
        Objects.requireNonNull(value, "EmpresaId no puede ser nulo (MT-01)");
    }
}
