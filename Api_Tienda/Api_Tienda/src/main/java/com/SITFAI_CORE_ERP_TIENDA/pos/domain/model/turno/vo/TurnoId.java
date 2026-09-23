package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador inmutable y fuertemente tipado para el Turno de Caja.
 */
public record TurnoId(UUID value) {

    public TurnoId {
        Objects.requireNonNull(value, "El ID del turno no puede ser nulo");
    }

    public static TurnoId generar() {
        return new TurnoId(UUID.randomUUID());
    }

    public static TurnoId of(UUID value) {
        return new TurnoId(value);
    }

    public static TurnoId fromString(String value) {
        Objects.requireNonNull(value, "El valor no puede ser nulo");
        return new TurnoId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
