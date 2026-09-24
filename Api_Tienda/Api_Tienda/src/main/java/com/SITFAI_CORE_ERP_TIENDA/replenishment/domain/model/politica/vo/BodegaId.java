package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo;

import java.util.Objects;
import java.util.UUID;

public record BodegaId(UUID valor) {
    public BodegaId {
        Objects.requireNonNull(valor, "El valor de BodegaId no puede ser nulo");
    }
}
