package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.util.UUID;

/**
 * Value Object: Identidad de la SolicitudAbastecimiento.
 * Inmutable por diseño (record de Java 21). Regla 3 — DDD.
 */
public record SolicitudId(UUID valor) {

    public SolicitudId {
        if (valor == null) {
            throw new IllegalArgumentException("El SolicitudId no puede ser nulo.");
        }
    }

    public static SolicitudId generar() {
        return new SolicitudId(UUID.randomUUID());
    }

    public static SolicitudId de(String uuid) {
        try {
            return new SolicitudId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("SolicitudId con formato UUID inválido: " + uuid, e);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
