package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record UsuarioId(UUID value) {
    public UsuarioId {
        Objects.requireNonNull(value, "UsuarioId no puede ser nulo (Cajero)");
    }
}
