package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo;

import java.util.Objects;
import java.util.UUID;

public record PoliticaId(UUID valor) {
    public PoliticaId {
        Objects.requireNonNull(valor, "El valor de PoliticaId no puede ser nulo");
    }
}
