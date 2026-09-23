package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador inmutable y fuertemente tipado para el Cajero u Operador responsable del turno.
 */
public record CajeroId(UUID value) {

    public CajeroId {
        Objects.requireNonNull(value, "El ID del cajero no puede ser nulo");
    }

    public static CajeroId generar() {
        return new CajeroId(UUID.randomUUID());
    }

    public static CajeroId of(UUID value) {
        return new CajeroId(value);
    }

    public static CajeroId fromString(String value) {
        Objects.requireNonNull(value, "El valor no puede ser nulo");
        return new CajeroId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
