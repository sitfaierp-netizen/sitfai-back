package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador inmutable y fuertemente tipado para la Empresa / Tenant (Regla MT-01).
 */
public record EmpresaId(UUID value) {

    public EmpresaId {
        Objects.requireNonNull(value, "El ID de la empresa no puede ser nulo");
    }

    public static EmpresaId of(UUID value) {
        return new EmpresaId(value);
    }

    public static EmpresaId fromString(String value) {
        Objects.requireNonNull(value, "El valor no puede ser nulo");
        return new EmpresaId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
