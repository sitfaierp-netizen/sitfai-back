package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando una Sucursal es desactivada (SUC-04, SUC-06).
 */
public record SucursalDesactivadaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        SucursalId sucursalId,
        Instant ocurridoEn
) implements DomainEvent {

    public SucursalDesactivadaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null.");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null.");
        Objects.requireNonNull(sucursalId, "sucursalId no puede ser null.");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null.");
    }

    public static SucursalDesactivadaEvent ahora(EmpresaId empresaId, SucursalId sucursalId) {
        return new SucursalDesactivadaEvent(UUID.randomUUID(), empresaId, sucursalId, Instant.now());
    }
}
