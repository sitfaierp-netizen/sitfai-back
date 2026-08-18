package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único inmutable del pedido de venta que origina la Factura.
 * <p>
 * Implementado como record de Java 25.
 */
public record PedidoOrigenId(UUID valor) {

    public PedidoOrigenId {
        Objects.requireNonNull(valor, "PedidoOrigenId: el valor UUID no puede ser null.");
    }

    public static PedidoOrigenId generar() {
        return new PedidoOrigenId(UUID.randomUUID());
    }

    public static PedidoOrigenId de(UUID valor) {
        return new PedidoOrigenId(valor);
    }

    public static PedidoOrigenId de(String valor) {
        Objects.requireNonNull(valor, "PedidoOrigenId: la cadena de texto no puede ser null.");
        try {
            return new PedidoOrigenId(UUID.fromString(valor.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("PedidoOrigenId: formato UUID inválido: " + valor, e);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
