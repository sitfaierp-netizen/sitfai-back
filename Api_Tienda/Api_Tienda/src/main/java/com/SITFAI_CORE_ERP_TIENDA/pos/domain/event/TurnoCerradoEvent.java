package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajeroId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio emitido al cerrar y arquear exitosamente un Turno de Caja (Reglas CAJ-05 a CAJ-07 y AUD-01).
 * <p>
 * Contiene el resumen financiero inmutable del arqueo de caja para trazabilidad y auditoría.
 */
public record TurnoCerradoEvent(
        UUID eventoId,
        TurnoId turnoId,
        EmpresaId empresaId,
        CajaId cajaId,
        CajeroId cajeroId,
        Dinero montoApertura,
        Dinero totalVentas,
        Dinero totalIngresos,
        Dinero totalDevoluciones,
        Dinero totalEgresos,
        Dinero totalTeoricoEsperado,
        Dinero montoFisicoDeclarado,
        Dinero descuadre,
        Instant ocurridoEn
) implements DomainEvent {

    public TurnoCerradoEvent {
        Objects.requireNonNull(eventoId, "El eventoId no puede ser nulo");
        Objects.requireNonNull(turnoId, "El turnoId no puede ser nulo");
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo (MT-01)");
        Objects.requireNonNull(cajaId, "El cajaId no puede ser nulo");
        Objects.requireNonNull(cajeroId, "El cajeroId no puede ser nulo");
        Objects.requireNonNull(montoApertura, "El montoApertura no puede ser nulo");
        Objects.requireNonNull(totalVentas, "El totalVentas no puede ser nulo");
        Objects.requireNonNull(totalIngresos, "El totalIngresos no puede ser nulo");
        Objects.requireNonNull(totalDevoluciones, "El totalDevoluciones no puede ser nulo");
        Objects.requireNonNull(totalEgresos, "El totalEgresos no puede ser nulo");
        Objects.requireNonNull(totalTeoricoEsperado, "El totalTeoricoEsperado no puede ser nulo");
        Objects.requireNonNull(montoFisicoDeclarado, "El montoFisicoDeclarado no puede ser nulo");
        Objects.requireNonNull(descuadre, "El descuadre no puede ser nulo");
        Objects.requireNonNull(ocurridoEn, "La fecha ocurridoEn no puede ser nula");
    }

    /**
     * Factory method canónico para el Agregado TurnoCaja de Outbound/POS.
     */
    public static TurnoCerradoEvent of(
            TurnoId turnoId,
            EmpresaId empresaId,
            CajaId cajaId,
            CajeroId cajeroId,
            Dinero montoApertura,
            Dinero totalVentas,
            Dinero totalIngresos,
            Dinero totalDevoluciones,
            Dinero totalEgresos,
            Dinero totalTeoricoEsperado,
            Dinero montoFisicoDeclarado,
            Dinero descuadre,
            Instant ocurridoEn
    ) {
        return new TurnoCerradoEvent(
                UUID.randomUUID(),
                turnoId,
                empresaId,
                cajaId,
                cajeroId,
                montoApertura,
                totalVentas,
                totalIngresos,
                totalDevoluciones,
                totalEgresos,
                totalTeoricoEsperado,
                montoFisicoDeclarado,
                descuadre,
                ocurridoEn != null ? ocurridoEn : Instant.now()
        );
    }


}
