package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de dominio emitido cuando una Empresa es suspendida operativamente (EMP-06).
 */
public record EmpresaSuspendidaEvent(
        UUID eventoId,
        EmpresaId empresaId,
        String motivo,
        Instant ocurridoEn
) implements DomainEvent {

    public EmpresaSuspendidaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null.");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null.");
        Objects.requireNonNull(motivo, "motivo no puede ser null.");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null.");
    }

    public static EmpresaSuspendidaEvent ahora(EmpresaId empresaId, String motivo) {
        return new EmpresaSuspendidaEvent(UUID.randomUUID(), empresaId, motivo, Instant.now());
    }
}
