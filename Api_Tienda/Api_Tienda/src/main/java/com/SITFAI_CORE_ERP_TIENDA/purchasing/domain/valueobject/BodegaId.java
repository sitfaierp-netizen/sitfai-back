package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.util.UUID;

/**
 * Value Object: Identidad de la Bodega destino de la solicitud.
 * Inmutable (record de Java 21). Referencia cruzada por ID — Regla 3 DDD.
 */
public record BodegaId(UUID valor) {

    public BodegaId {
        if (valor == null) {
            throw new IllegalArgumentException("El BodegaId no puede ser nulo.");
        }
    }

    public static BodegaId de(String uuid) {
        try {
            return new BodegaId(UUID.fromString(uuid));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("BodegaId con formato UUID inválido: " + uuid, e);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
