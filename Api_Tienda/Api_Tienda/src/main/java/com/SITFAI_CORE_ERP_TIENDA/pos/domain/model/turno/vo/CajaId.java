package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador inmutable y fuertemente tipado para la Caja Registradora física/lógica.
 */
public record CajaId(UUID value) {

    public CajaId {
        Objects.requireNonNull(value, "El ID de la caja no puede ser nulo");
    }

    public static CajaId generar() {
        return new CajaId(UUID.randomUUID());
    }

    public static CajaId of(UUID value) {
        return new CajaId(value);
    }

    public static CajaId fromString(String value) {
        Objects.requireNonNull(value, "El valor no puede ser nulo");
        return new CajaId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
