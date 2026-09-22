package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object inmutable: Identificador único de una Orden de Compra (BOD-04).
 */
public record OrdenCompraId(UUID valor) {

    public OrdenCompraId {
        Objects.requireNonNull(valor, "OrdenCompraId: el UUID no puede ser null.");
    }

    public static OrdenCompraId generar() {
        return new OrdenCompraId(UUID.randomUUID());
    }

    public static OrdenCompraId de(UUID valor) {
        return new OrdenCompraId(valor);
    }

    public static OrdenCompraId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("OrdenCompraId: la representación String no puede ser null ni vacía.");
        }
        return new OrdenCompraId(UUID.fromString(valor.trim()));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
