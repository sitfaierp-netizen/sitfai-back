package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo;

import java.util.UUID;

public record DevolucionId(UUID valor) {
    public DevolucionId {
        if (valor == null) throw new IllegalArgumentException("El id de devolución no puede ser nulo");
    }
    public static DevolucionId generar() {
        return new DevolucionId(UUID.randomUUID());
    }
    public static DevolucionId de(UUID id) {
        return new DevolucionId(id);
    }
}
