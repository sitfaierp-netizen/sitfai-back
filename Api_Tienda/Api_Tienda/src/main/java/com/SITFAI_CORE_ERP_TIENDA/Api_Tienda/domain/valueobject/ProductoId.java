package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject;

import java.util.UUID;

/**
 * Value Object: Identificador único de un Producto dentro del catálogo.
 * <p>
 * Inmutable por diseño (record Java 25).
 * Se utiliza como referencia entre Bounded Contexts (REGLA-3).
 */
public record ProductoId(UUID valor) {

    public ProductoId {
        if (valor == null) {
            throw new IllegalArgumentException("ProductoId: el UUID no puede ser null.");
        }
    }

    public static ProductoId generar() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("ProductoId: el UUID no puede ser null.");
        }
        return new ProductoId(valor);
    }

    public static ProductoId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("ProductoId: la representación String no puede ser null ni vacía.");
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
