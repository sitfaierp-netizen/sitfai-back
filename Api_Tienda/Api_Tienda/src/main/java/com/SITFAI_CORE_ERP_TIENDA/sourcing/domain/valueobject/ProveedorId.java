package com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject;

import java.util.UUID;

/** VO inmutable: Identidad del Proveedor (record Java 21). */
public record ProveedorId(UUID valor) {
    public ProveedorId {
        if (valor == null) throw new IllegalArgumentException("ProveedorId no puede ser nulo.");
    }
    public static ProveedorId generar() { return new ProveedorId(UUID.randomUUID()); }
    public static ProveedorId de(String uuid) {
        try { return new ProveedorId(UUID.fromString(uuid)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("ProveedorId UUID inválido: " + uuid, e); }
    }
    @Override public String toString() { return valor.toString(); }
}
