package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identidad única de un Análisis ABC.
 * <p>
 * Regla REGLA-3: ID de tipo Value Object (no Long primitivo).
 * Regla REGLA-1: Record Java 21 — inmutable, sin dependencias de frameworks.
 */
public record AnalisisId(UUID valor) {

    public AnalisisId {
        Objects.requireNonNull(valor, "AnalisisId: el UUID no puede ser null.");
    }

    /** Factory method de generación. */
    public static AnalisisId generar() {
        return new AnalisisId(UUID.randomUUID());
    }

    /** Factory method desde String UUID. */
    public static AnalisisId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("AnalisisId: la representación String no puede ser null ni vacía.");
        }
        try {
            return new AnalisisId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("AnalisisId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
