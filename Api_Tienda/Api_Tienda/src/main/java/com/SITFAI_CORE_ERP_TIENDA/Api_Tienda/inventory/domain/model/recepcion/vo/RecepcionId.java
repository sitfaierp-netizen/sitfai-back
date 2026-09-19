package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo;

import java.util.UUID;

/**
 * Value Object: Identificador único de una Recepción.
 */
public record RecepcionId(UUID valor) {
    public RecepcionId {
        if (valor == null) {
            throw new IllegalArgumentException("RecepcionId: el UUID no puede ser null.");
        }
    }

    public static RecepcionId generar() {
        return new RecepcionId(UUID.randomUUID());
    }
}
