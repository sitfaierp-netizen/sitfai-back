package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador del Producto incluido en las líneas del pedido.
 * <p>
 * Regla REGLA-3: Record Java 21 inmutable.
 */
public record ProductoId(UUID valor) {

    public ProductoId {
        Objects.requireNonNull(valor, "ProductoId: el UUID no puede ser nulo.");
    }

    public static ProductoId generar() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId de(UUID valor) {
        return new ProductoId(valor);
    }

    public static ProductoId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("ProductoId: la representación String no puede ser nula ni vacía.");
        }
        try {
            return new ProductoId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("ProductoId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
