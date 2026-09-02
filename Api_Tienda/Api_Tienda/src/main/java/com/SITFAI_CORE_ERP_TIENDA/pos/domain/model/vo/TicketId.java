package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: Identificador único de un Ticket de Venta POS.
 * Inmutable, fail-fast.
 */
public record TicketId(UUID value) {

    public TicketId {
        Objects.requireNonNull(value, "El TicketId no puede ser nulo");
    }

    public static TicketId generar() {
        return new TicketId(UUID.randomUUID());
    }

    public static TicketId de(String value) {
        Objects.requireNonNull(value, "El TicketId string no puede ser nulo");
        return new TicketId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
