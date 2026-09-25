package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador de la Bodega donde se ejecuta el conteo cíclico.
 * <p>
 * Regla REGLA-3: Record inmutable de Java 21.
 * Regla REGLA-1: Cero dependencias de frameworks.
 */
public record BodegaId(UUID valor) {

    public BodegaId {
        Objects.requireNonNull(valor, "BodegaId: el UUID no puede ser nulo.");
    }

    public static BodegaId generar() {
        return new BodegaId(UUID.randomUUID());
    }

    public static BodegaId de(UUID valor) {
        return new BodegaId(valor);
    }

    public static BodegaId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("BodegaId: la representación String no puede ser nula ni vacía.");
        }
        try {
            return new BodegaId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("BodegaId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
