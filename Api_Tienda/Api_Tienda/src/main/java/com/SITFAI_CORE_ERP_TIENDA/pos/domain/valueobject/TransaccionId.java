package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de una Transacción de Caja (CAJ-07).
 * <p>
 * Java 25 Record inmutable.
 */
public record TransaccionId(UUID valor) {

    public TransaccionId {
        Objects.requireNonNull(valor, "TransaccionId: el valor UUID no puede ser null.");
    }

    public static TransaccionId generar() {
        return new TransaccionId(UUID.randomUUID());
    }

    public static TransaccionId de(UUID valor) {
        return new TransaccionId(valor);
    }

    public static TransaccionId de(String valor) {
        Objects.requireNonNull(valor, "TransaccionId: el valor string no puede ser null.");
        return new TransaccionId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
