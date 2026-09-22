package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object inmutable: Identificador del Proveedor adjudicado para la Orden de Compra.
 */
public record ProveedorId(UUID valor) {

    public ProveedorId {
        Objects.requireNonNull(valor, "ProveedorId: el UUID no puede ser null.");
    }

    public static ProveedorId generar() {
        return new ProveedorId(UUID.randomUUID());
    }

    public static ProveedorId de(UUID valor) {
        return new ProveedorId(valor);
    }

    public static ProveedorId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("ProveedorId: la representación String no puede ser null ni vacía.");
        }
        return new ProveedorId(UUID.fromString(valor.trim()));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
