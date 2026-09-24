package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo;

import java.util.Objects;
import java.util.UUID;

public record ProductoId(UUID valor) {
    public ProductoId {
        Objects.requireNonNull(valor, "El valor de ProductoId no puede ser nulo");
    }
}
