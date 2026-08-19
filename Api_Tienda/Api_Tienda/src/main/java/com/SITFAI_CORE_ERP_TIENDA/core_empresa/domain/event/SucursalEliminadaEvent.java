package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SucursalEliminadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        SucursalId sucursalId
) implements DomainEvent {

    public SucursalEliminadaEvent {
        Objects.requireNonNull(eventoId);
        Objects.requireNonNull(ocurridoEn);
        Objects.requireNonNull(empresaId);
        Objects.requireNonNull(sucursalId);
    }

    public static SucursalEliminadaEvent ahora(EmpresaId empresaId, SucursalId sucursalId) {
        return new SucursalEliminadaEvent(UUID.randomUUID(), Instant.now(), empresaId, sucursalId);
    }
}
