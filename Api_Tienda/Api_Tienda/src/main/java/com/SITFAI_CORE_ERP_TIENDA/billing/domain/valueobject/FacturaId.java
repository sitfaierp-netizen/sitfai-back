package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único inmutable de una {@code Factura}.
 * <p>
 * Implementado como record de Java 25.
 */
public record FacturaId(UUID valor) {

    public FacturaId {
        Objects.requireNonNull(valor, "FacturaId: el valor UUID no puede ser null.");
    }

    public static FacturaId generar() {
        return new FacturaId(UUID.randomUUID());
    }

    public static FacturaId de(UUID valor) {
        return new FacturaId(valor);
    }

    public static FacturaId de(String valor) {
        Objects.requireNonNull(valor, "FacturaId: la cadena de texto no puede ser null.");
        try {
            return new FacturaId(UUID.fromString(valor.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("FacturaId: formato UUID inválido: " + valor, e);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
