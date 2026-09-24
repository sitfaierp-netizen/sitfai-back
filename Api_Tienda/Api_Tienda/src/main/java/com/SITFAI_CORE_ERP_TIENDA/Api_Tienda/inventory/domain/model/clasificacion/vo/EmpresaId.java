package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador de la Empresa (Tenant) en el contexto analítico ABC.
 * <p>
 * Regla MT-01: El empresaId es la clave de partición raíz de todo dato analítico.
 * Regla REGLA-3: Record Java 21 — inmutable y con validación fail-fast.
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        Objects.requireNonNull(valor, "EmpresaId: el UUID no puede ser null (MT-01).");
    }

    public static EmpresaId de(UUID valor) {
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
