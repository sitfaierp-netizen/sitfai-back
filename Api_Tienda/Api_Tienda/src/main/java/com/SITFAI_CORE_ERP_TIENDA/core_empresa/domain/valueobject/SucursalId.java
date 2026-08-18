package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de Sucursal (SUC-01).
 * Inmutable (Record de Java 25).
 */
public record SucursalId(UUID valor) {

    public SucursalId {
        Objects.requireNonNull(valor, "SucursalId no puede ser null.");
    }

    public static SucursalId generar() {
        return new SucursalId(UUID.randomUUID());
    }

    public static SucursalId de(UUID valor) {
        return new SucursalId(valor);
    }

    public static SucursalId de(String valor) {
        Objects.requireNonNull(valor, "SucursalId en String no puede ser null.");
        return new SucursalId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
