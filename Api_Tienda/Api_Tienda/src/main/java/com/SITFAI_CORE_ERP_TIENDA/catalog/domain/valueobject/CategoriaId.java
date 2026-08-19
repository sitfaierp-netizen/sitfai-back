package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject;

import java.util.UUID;

/** VO inmutable: Identidad de la Categoría (record Java 21). */
public record CategoriaId(UUID valor) {
    public CategoriaId {
        if (valor == null) throw new IllegalArgumentException("CategoriaId no puede ser nulo.");
    }
    public static CategoriaId generar() { return new CategoriaId(UUID.randomUUID()); }
    public static CategoriaId de(String uuid) {
        try { return new CategoriaId(UUID.fromString(uuid)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("CategoriaId UUID inválido: " + uuid, e); }
    }
    @Override public String toString() { return valor.toString(); }
}
