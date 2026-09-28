package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Emitido cuando se inicia exitosamente un nuevo Turno de Caja con un monto inicial declarado.
 * <p>
 * Reglas aplicadas: CAJ-03 (monto inicial obligatorio), MT-01 (multitenancy).
 */
public record TurnoAbiertoEvent(
        UUID eventoId,
        TurnoId turnoId,
        CajaId cajaId,
        EmpresaId empresaId,
        Dinero montoInicial,
        Instant ocurridoEn
) implements DomainEvent {

    public TurnoAbiertoEvent {
        Objects.requireNonNull(eventoId, "TurnoAbiertoEvent: eventoId es obligatorio.");
        Objects.requireNonNull(turnoId, "TurnoAbiertoEvent: turnoId es obligatorio.");
        Objects.requireNonNull(cajaId, "TurnoAbiertoEvent: cajaId es obligatorio.");
        Objects.requireNonNull(empresaId, "TurnoAbiertoEvent: empresaId es obligatorio.");
        Objects.requireNonNull(montoInicial, "TurnoAbiertoEvent: montoInicial es obligatorio.");
        Objects.requireNonNull(ocurridoEn, "TurnoAbiertoEvent: ocurridoEn es obligatorio.");
    }

    public static TurnoAbiertoEvent of(TurnoId turnoId, CajaId cajaId, EmpresaId empresaId, Dinero montoInicial) {
        return new TurnoAbiertoEvent(
                UUID.randomUUID(),
                turnoId,
                cajaId,
                empresaId,
                montoInicial,
                Instant.now()
        );
    }

    public String tipoEvento() {
        return "pos.turno.abierto";
    }
}
