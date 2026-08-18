package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de una Bodega.
 * <p>
 * Inmutable por diseño (record Java 25).
 * Soporta UUIDs para entornos distribuidos multi-nodo (BOD-01).
 */
public record BodegaId(UUID valor) {

    public BodegaId {
        if (valor == null) {
            throw new IllegalArgumentException("BodegaId: el UUID no puede ser null.");
        }
    }

    public static BodegaId generar() {
        return new BodegaId(UUID.randomUUID());
    }

    public static BodegaId nuevo() {
        return generar();
    }

    public static BodegaId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("BodegaId: el UUID no puede ser null.");
        }
        return new BodegaId(valor);
    }

    public static BodegaId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("BodegaId: la representación String no puede ser null ni vacía.");
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
