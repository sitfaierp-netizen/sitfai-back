package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando una Sucursal inactiva es reactivada (SUC-04).
 */
public record SucursalActivadaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        SucursalId sucursalId,
        Instant ocurridoEn
) implements DomainEvent {

    public SucursalActivadaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null.");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null.");
        Objects.requireNonNull(sucursalId, "sucursalId no puede ser null.");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null.");
    }

    public static SucursalActivadaEvent ahora(EmpresaId empresaId, SucursalId sucursalId) {
        return new SucursalActivadaEvent(UUID.randomUUID(), empresaId, sucursalId, Instant.now());
    }
}
