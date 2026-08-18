package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único inmutable de la Empresa / Tenant (MT-01).
 * <p>
 * Discriminador obligatorio para el aislamiento multitenant.
 * Implementado como record de Java 25.
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        Objects.requireNonNull(valor, "EmpresaId: el valor UUID no puede ser null (MT-01).");
    }

    public static EmpresaId generar() {
        return new EmpresaId(UUID.randomUUID());
    }

    public static EmpresaId de(UUID valor) {
        return new EmpresaId(valor);
    }

    public static EmpresaId de(String valor) {
        Objects.requireNonNull(valor, "EmpresaId: la cadena de texto no puede ser null.");
        try {
            return new EmpresaId(UUID.fromString(valor.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("EmpresaId: formato UUID inválido: " + valor, e);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
