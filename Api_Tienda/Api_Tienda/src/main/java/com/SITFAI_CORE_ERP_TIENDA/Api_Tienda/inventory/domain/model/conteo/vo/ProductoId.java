package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador del Producto auditado en el conteo cíclico.
 * <p>
 * Regla REGLA-3: Record inmutable de Java 21.
 * Regla REGLA-1: Cero dependencias de frameworks.
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
