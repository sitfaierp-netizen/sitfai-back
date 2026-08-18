package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de Empresa (Tenant Raíz / EMP-01).
 * Inmutable (Record de Java 25).
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        Objects.requireNonNull(valor, "EmpresaId no puede ser null.");
    }

    public static EmpresaId generar() {
        return new EmpresaId(UUID.randomUUID());
    }

    public static EmpresaId de(UUID valor) {
        return new EmpresaId(valor);
    }

    public static EmpresaId de(String valor) {
        Objects.requireNonNull(valor, "EmpresaId en String no puede ser null.");
        return new EmpresaId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
