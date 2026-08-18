package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de la Sucursal.
 * <p>
 * Inmutable por diseño (record Java 25).
 * Soporta UUIDs para entornos distribuidos multi-nodo (BOD-01).
 */
public record SucursalId(UUID valor) {

    public SucursalId {
        if (valor == null) {
            throw new IllegalArgumentException("SucursalId: el UUID no puede ser null.");
        }
    }

    public static SucursalId generar() {
        return new SucursalId(UUID.randomUUID());
    }

    public static SucursalId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("SucursalId: el UUID no puede ser null.");
        }
        return new SucursalId(valor);
    }

    public static SucursalId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("SucursalId: la representación String no puede ser null ni vacía.");
        }
        try {
            return new SucursalId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("SucursalId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
