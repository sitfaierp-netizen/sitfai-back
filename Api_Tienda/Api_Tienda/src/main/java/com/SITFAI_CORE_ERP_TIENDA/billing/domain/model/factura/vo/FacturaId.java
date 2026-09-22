package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único inmutable de una Factura.
 * <p>
 * Record puro de Java 21 (REGLA-3).
 */
public record FacturaId(UUID valor) {

    public FacturaId {
        Objects.requireNonNull(valor, "FacturaId: el UUID no puede ser null.");
    }

    public static FacturaId generar() {
        return new FacturaId(UUID.randomUUID());
    }

    public static FacturaId de(UUID valor) {
        return new FacturaId(valor);
    }

    public static FacturaId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("FacturaId: la representación String no puede ser null ni vacía.");
        }
        return new FacturaId(UUID.fromString(valor.trim()));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
