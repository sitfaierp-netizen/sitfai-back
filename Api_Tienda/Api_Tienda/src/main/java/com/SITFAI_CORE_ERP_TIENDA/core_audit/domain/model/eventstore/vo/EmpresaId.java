package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object inmutable: Identificador único del Tenant (Empresa).
 * <p>
 * Regla MT-01: El campo empresa_id es el discriminador raíz inquebrantable de aislamiento multi-tenant.
 * Toda traza o evento registrado en el Event Store debe estar inexorablemente particionado por su EmpresaId.
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        Objects.requireNonNull(valor, "EmpresaId: el UUID no puede ser null (MT-01).");
    }

    public static EmpresaId generar() {
        return new EmpresaId(UUID.randomUUID());
    }

    public static EmpresaId de(UUID valor) {
        return new EmpresaId(valor);
    }

    public static EmpresaId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("EmpresaId: la representación String no puede ser null ni vacía (MT-01).");
        }
        return new EmpresaId(UUID.fromString(valor.trim()));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
