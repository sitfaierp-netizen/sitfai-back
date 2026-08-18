package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de la Empresa (Tenant Raíz).
 * <p>
 * Inmutable por diseño (record Java 25). Discriminador de multitenancy (REGLA-4, MT-01).
 * Soporta UUIDs para entornos distribuidos multi-nodo.
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        if (valor == null) {
            throw new IllegalArgumentException("EmpresaId: el UUID no puede ser null.");
        }
    }

    public static EmpresaId generar() {
        return new EmpresaId(UUID.randomUUID());
    }

    public static EmpresaId de(UUID valor) {
        if (valor == null) {
            throw new IllegalArgumentException("EmpresaId: el UUID no puede ser null.");
        }
        return new EmpresaId(valor);
    }

    public static EmpresaId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("EmpresaId: la representación String no puede ser null ni vacía.");
        }
        try {
            return new EmpresaId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("EmpresaId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
