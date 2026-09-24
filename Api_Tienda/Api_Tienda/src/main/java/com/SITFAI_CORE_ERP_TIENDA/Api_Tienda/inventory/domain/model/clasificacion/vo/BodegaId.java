package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador de la Bodega en el contexto analítico ABC.
 * <p>
 * La clasificación ABC es siempre específica a una bodega y un tenant (MT-01).
 * Regla REGLA-1: Record Java 21 — inmutable y sin dependencias de frameworks.
 */
public record BodegaId(UUID valor) {

    public BodegaId {
        Objects.requireNonNull(valor, "BodegaId (ABC): el UUID no puede ser null.");
    }

    public static BodegaId de(UUID valor) {
        return new BodegaId(valor);
    }

    public static BodegaId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("BodegaId (ABC): la representación String no puede ser null ni vacía.");
        }
        try {
            return new BodegaId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("BodegaId (ABC): formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
