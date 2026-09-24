package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador del Producto en el contexto analítico ABC.
 * <p>
 * Referencia cruzada por ID — el dominio ABC nunca instancia el Agregado Producto completo (REGLA-3).
 * Regla REGLA-1: Record Java 21 — inmutable y sin dependencias de frameworks.
 */
public record ProductoId(UUID valor) {

    public ProductoId {
        Objects.requireNonNull(valor, "ProductoId (ABC): el UUID no puede ser null.");
    }

    public static ProductoId de(UUID valor) {
        return new ProductoId(valor);
    }

    public static ProductoId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("ProductoId (ABC): la representación String no puede ser null ni vacía.");
        }
        try {
            return new ProductoId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("ProductoId (ABC): formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
