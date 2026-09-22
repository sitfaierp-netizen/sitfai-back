package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único inmutable del Cliente receptor de la Factura.
 * <p>
 * Record puro de Java 21 (REGLA-3).
 */
public record ClienteId(UUID valor) {

    public ClienteId {
        Objects.requireNonNull(valor, "ClienteId: el UUID no puede ser null.");
    }

    public static ClienteId generar() {
        return new ClienteId(UUID.randomUUID());
    }

    public static ClienteId de(UUID valor) {
        return new ClienteId(valor);
    }

    public static ClienteId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("ClienteId: la representación String no puede ser null ni vacía.");
        }
        return new ClienteId(UUID.fromString(valor.trim()));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
