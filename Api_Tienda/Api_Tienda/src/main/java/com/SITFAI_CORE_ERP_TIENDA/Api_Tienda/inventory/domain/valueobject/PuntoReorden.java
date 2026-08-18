package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Representa el punto de reorden (Replenishment) de un producto en la Bodega.
 * Inmutable (Puro Java 25).
 */
public record PuntoReorden(BigDecimal valor) {

    public PuntoReorden {
        Objects.requireNonNull(valor, "PuntoReorden: El valor no puede ser nulo.");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("PuntoReorden: El valor no puede ser negativo.");
        }
    }

    public static PuntoReorden de(BigDecimal valor) {
        return new PuntoReorden(valor);
    }

    public static PuntoReorden porDefecto() {
        return new PuntoReorden(BigDecimal.ZERO);
    }
}
