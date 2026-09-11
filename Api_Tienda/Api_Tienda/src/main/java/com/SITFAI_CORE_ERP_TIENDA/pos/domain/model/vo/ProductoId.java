package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador de Producto usado en contexto POS.
 * Inmutable, fail-fast.
 */
public record ProductoId(UUID value) {

    public ProductoId {
        Objects.requireNonNull(value, "El ProductoId no puede ser nulo");
    }

    public static ProductoId de(String value) {
        Objects.requireNonNull(value, "El ProductoId string no puede ser nulo");
        return new ProductoId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
