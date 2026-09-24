package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador del Cliente que realiza el pedido.
 * <p>
 * Regla REGLA-3: Record Java 21 inmutable. Referencia cruzada por ID sin acoplar agregados.
 */
public record ClienteId(UUID valor) {

    public ClienteId {
        Objects.requireNonNull(valor, "ClienteId: el UUID no puede ser nulo.");
    }

    public static ClienteId generar() {
        return new ClienteId(UUID.randomUUID());
    }

    public static ClienteId de(UUID valor) {
        return new ClienteId(valor);
    }

    public static ClienteId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("ClienteId: la representación String no puede ser nula ni vacía.");
        }
        try {
            return new ClienteId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("ClienteId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
