package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model;

import java.util.UUID;

/**
 * Tenant identifier owned by the idempotency bounded context.
 *
 * <p>The core idempotency model must not depend on a value object from a
 * business context such as Inventory.</p>
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        if (valor == null) {
            throw new IllegalArgumentException("EmpresaId: el UUID no puede ser null.");
        }
    }

    public static EmpresaId de(UUID valor) {
        return new EmpresaId(valor);
    }

    public static EmpresaId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("EmpresaId: el valor no puede ser null ni vacío.");
        }
        return new EmpresaId(UUID.fromString(valor.trim()));
    }

    public static EmpresaId generar() {
        return new EmpresaId(UUID.randomUUID());
    }
}
