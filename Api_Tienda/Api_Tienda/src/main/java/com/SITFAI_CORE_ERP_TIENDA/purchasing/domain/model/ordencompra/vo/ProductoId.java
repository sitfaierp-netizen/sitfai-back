package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object inmutable: Identificador del Producto a adquirir en la Orden de Compra.
 */
public record ProductoId(UUID valor) {

    public ProductoId {
        Objects.requireNonNull(valor, "ProductoId: el UUID no puede ser null.");
    }

    public static ProductoId generar() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId de(UUID valor) {
        return new ProductoId(valor);
    }

    public static ProductoId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("ProductoId: la representación String no puede ser null ni vacía.");
        }
        return new ProductoId(UUID.fromString(valor.trim()));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
