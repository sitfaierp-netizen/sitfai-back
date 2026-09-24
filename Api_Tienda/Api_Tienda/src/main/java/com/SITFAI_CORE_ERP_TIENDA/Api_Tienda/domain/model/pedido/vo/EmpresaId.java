package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador de la Empresa (Tenant).
 * <p>
 * Regla MT-01: El empresaId es la clave de partición raíz obligatoria en todo agregado y operación.
 * Regla REGLA-3: Record Java 21 inmutable.
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        Objects.requireNonNull(valor, "EmpresaId: el UUID no puede ser nulo (MT-01).");
    }

    public static EmpresaId generar() {
        return new EmpresaId(UUID.randomUUID());
    }

    public static EmpresaId de(UUID valor) {
        return new EmpresaId(valor);
    }

    public static EmpresaId de(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("EmpresaId: la representación String no puede ser nula ni vacía.");
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
