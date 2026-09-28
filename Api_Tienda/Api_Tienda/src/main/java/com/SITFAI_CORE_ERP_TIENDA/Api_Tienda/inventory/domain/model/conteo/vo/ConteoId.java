package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identidad única de un Conteo Cíclico de Inventario.
 * <p>
 * Regla REGLA-3: ID de tipo Value Object, inmutable (record Java 21).
 * Regla REGLA-1: Cero dependencias de frameworks externos.
 */
public record ConteoId(UUID valor) {

    public ConteoId {
        Objects.requireNonNull(valor, "ConteoId: el UUID no puede ser nulo.");
    }

    public static ConteoId generar() {
        return new ConteoId(UUID.randomUUID());
    }

    public static ConteoId de(UUID valor) {
        return new ConteoId(valor);
    }

    public static ConteoId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("ConteoId: la representación String no puede ser nula ni vacía.");
        }
        try {
            return new ConteoId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("ConteoId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
