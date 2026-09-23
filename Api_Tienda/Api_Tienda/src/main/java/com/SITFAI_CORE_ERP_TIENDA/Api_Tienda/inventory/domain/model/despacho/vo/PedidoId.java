package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Referencia cruzada al origen de E-commerce / Pedidos de venta.
 * <p>
 * Regla BOD-04: Todo movimiento de inventario / despacho exige un documento fuente traceable.
 * Inmutable por diseño (record Java 21).
 */
public record PedidoId(UUID valor) {

    public PedidoId {
        Objects.requireNonNull(valor, "PedidoId: el UUID no puede ser nulo (BOD-04).");
    }

    public static PedidoId de(UUID valor) {
        return new PedidoId(valor);
    }

    public static PedidoId de(String uuid) {
        Objects.requireNonNull(uuid, "PedidoId: la representación String no puede ser nula ni vacía.");
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
