package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject;

import java.util.UUID;

/** VO inmutable: Identidad del Producto (record Java 21). */
public record ProductoId(UUID valor) {
    public ProductoId {
        if (valor == null) throw new IllegalArgumentException("ProductoId no puede ser nulo.");
    }
    public static ProductoId generar() { return new ProductoId(UUID.randomUUID()); }
    public static ProductoId de(String uuid) {
        try { return new ProductoId(UUID.fromString(uuid)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("ProductoId UUID inválido: " + uuid, e); }
    }
    @Override public String toString() { return valor.toString(); }
}
