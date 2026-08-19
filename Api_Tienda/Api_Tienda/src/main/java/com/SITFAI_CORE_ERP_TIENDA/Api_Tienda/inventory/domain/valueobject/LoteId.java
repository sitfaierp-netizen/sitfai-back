package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject;

import java.util.Objects;

/**
 * Value Object: Representa el identificador único de un Lote de productos.
 * Inmutable.
 */
public record LoteId(String valor) {

    public LoteId {
        Objects.requireNonNull(valor, "LoteId: El valor no puede ser nulo.");
        if (valor.isBlank()) {
            throw new IllegalArgumentException("LoteId: El valor no puede estar vacío.");
        }
    }

    public static LoteId de(String valor) {
        return new LoteId(valor);
    }
}
