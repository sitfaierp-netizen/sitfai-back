package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EmpresaEliminadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId
) implements DomainEvent {

    public EmpresaEliminadaEvent {
        Objects.requireNonNull(eventoId);
        Objects.requireNonNull(ocurridoEn);
        Objects.requireNonNull(empresaId);
    }

    public static EmpresaEliminadaEvent ahora(EmpresaId empresaId) {
        return new EmpresaEliminadaEvent(UUID.randomUUID(), Instant.now(), empresaId);
    }
}
