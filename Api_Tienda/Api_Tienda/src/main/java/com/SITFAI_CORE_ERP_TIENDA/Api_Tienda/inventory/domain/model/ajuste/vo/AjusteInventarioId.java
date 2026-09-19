package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de un Ajuste de Inventario.
 */
public record AjusteInventarioId(UUID valor) {
    public AjusteInventarioId {
        Objects.requireNonNull(valor, "AjusteInventarioId: el UUID no puede ser null.");
    }

    public static AjusteInventarioId generar() {
        return new AjusteInventarioId(UUID.randomUUID());
    }

    public static AjusteInventarioId de(UUID valor) {
        return new AjusteInventarioId(valor);
    }

    public static AjusteInventarioId de(String uuid) {
        return new AjusteInventarioId(UUID.fromString(uuid));
    }
}
