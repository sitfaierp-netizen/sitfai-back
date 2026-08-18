package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando una Empresa suspendida es reactivada.
 */
public record EmpresaReactivadaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        Instant ocurridoEn
) implements DomainEvent {

    public EmpresaReactivadaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null.");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null.");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null.");
    }

    public static EmpresaReactivadaEvent ahora(EmpresaId empresaId) {
        return new EmpresaReactivadaEvent(UUID.randomUUID(), empresaId, Instant.now());
    }
}
