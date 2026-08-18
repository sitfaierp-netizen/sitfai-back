package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject;

import java.util.UUID;

/**
 * Value Object: Identificador único de un Cliente.
 * <p>
 * Inmutable por diseño (record Java 25).
 * Se pasa como referencia entre Bounded Contexts (REGLA-3).
 */
public record ClienteId(UUID valor) {

    public ClienteId {
        if (valor == null) {
            throw new IllegalArgumentException("ClienteId: el UUID no puede ser null.");
        }
    }

    public static ClienteId generar() {
        return new ClienteId(UUID.randomUUID());
    }

    public static ClienteId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("ClienteId: el UUID no puede ser null.");
        }
        return new ClienteId(valor);
    }

    public static ClienteId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("ClienteId: la representación String no puede ser null ni vacía.");
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
