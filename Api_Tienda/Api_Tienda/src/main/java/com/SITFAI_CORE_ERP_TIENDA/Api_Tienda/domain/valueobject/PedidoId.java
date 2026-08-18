package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de un Pedido de venta.
 * <p>
 * Inmutable por diseño (record Java 25).
 * Soporta UUIDs para entornos distribuidos multi-nodo.
 */
public record PedidoId(UUID valor) {

    public PedidoId {
        if (valor == null) {
            throw new IllegalArgumentException("PedidoId: el UUID no puede ser null.");
        }
    }

    public static PedidoId generar() {
        return new PedidoId(UUID.randomUUID());
    }

    public static PedidoId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("PedidoId: el UUID no puede ser null.");
        }
        return new PedidoId(valor);
    }

    public static PedidoId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("PedidoId: la representación String no puede ser null ni vacía.");
        }
        try {
            return new PedidoId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("PedidoId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
