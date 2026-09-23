package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único del Despacho (Outbound Logistics).
 * <p>
 * Inmutable por diseño (record Java 21). Regla 1 (Clean Architecture): Cero dependencias de frameworks.
 */
public record DespachoId(UUID valor) {

    public DespachoId {
        Objects.requireNonNull(valor, "DespachoId: el UUID no puede ser nulo.");
    }

    public static DespachoId generar() {
        return new DespachoId(UUID.randomUUID());
    }

    public static DespachoId de(UUID valor) {
        return new DespachoId(valor);
    }

    public static DespachoId de(String uuid) {
        Objects.requireNonNull(uuid, "DespachoId: la representación String no puede ser nula ni vacía.");
        try {
            return new DespachoId(UUID.fromString(uuid.trim()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("DespachoId: formato de UUID inválido: " + uuid, ex);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
