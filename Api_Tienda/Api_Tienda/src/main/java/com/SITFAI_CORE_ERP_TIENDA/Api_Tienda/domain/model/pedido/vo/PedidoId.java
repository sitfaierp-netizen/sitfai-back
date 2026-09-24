package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único del Pedido de Venta.
 * <p>
 * Regla REGLA-3: ID de tipo Value Object, inmutable (record Java 21).
 * Regla REGLA-1: Cero dependencias de frameworks externos.
 */
public record PedidoId(UUID valor) {

    public PedidoId {
        Objects.requireNonNull(valor, "PedidoId: el UUID no puede ser nulo.");
    }

    public static PedidoId generar() {
        return new PedidoId(UUID.randomUUID());
    }

    public static PedidoId de(UUID valor) {
        return new PedidoId(valor);
    }

    public static PedidoId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("PedidoId: la representación String no puede ser nula ni vacía.");
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
