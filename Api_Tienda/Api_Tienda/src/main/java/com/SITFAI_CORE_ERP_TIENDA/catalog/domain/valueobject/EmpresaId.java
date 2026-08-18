package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject;

import java.util.UUID;

/** VO inmutable: Identidad del Tenant (MT-01). */
public record EmpresaId(UUID valor) {
    public EmpresaId {
        if (valor == null) throw new IllegalArgumentException("EmpresaId no puede ser nulo (MT-01).");
    }
    public static EmpresaId de(UUID uuid) { return new EmpresaId(uuid); }
    @Override public String toString() { return valor.toString(); }
}
