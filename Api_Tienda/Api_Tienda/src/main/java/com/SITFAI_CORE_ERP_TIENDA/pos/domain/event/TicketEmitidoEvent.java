package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.LineaTicket;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.MetodoPago;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.TicketId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: TicketEmitidoEvent.
 *
 * CRÍTICO para integración con Inventario:
 * Al ser consumido por el módulo de Inventario, desencadena la deducción FEFO
 * de los lotes correspondientes a cada producto en la línea del ticket (INV-01).
 *
 * También puede ser consumido por Billing para la emisión de una Factura si el
 * cliente la solicita en ese momento.
 */
public record TicketEmitidoEvent(
        String eventId,
        TicketId ticketId,
        UUID empresaId,
        CajaId cajaId,
        TurnoId turnoId,
        List<LineaTicket> lineas,
        Dinero totalGeneral,
        MetodoPago metodoPago,
        Instant occurredOn
) implements DomainEvent {

    public TicketEmitidoEvent(TicketId ticketId, UUID empresaId, CajaId cajaId, TurnoId turnoId,
                              List<LineaTicket> lineas, Dinero totalGeneral, MetodoPago metodoPago) {
        this(
                UUID.randomUUID().toString(),
                Objects.requireNonNull(ticketId, "TicketId es obligatorio"),
                Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)"),
                Objects.requireNonNull(cajaId, "CajaId es obligatorio"),
                Objects.requireNonNull(turnoId, "TurnoId es obligatorio"),
                Objects.requireNonNull(lineas, "Las líneas son obligatorias"),
                Objects.requireNonNull(totalGeneral, "El total es obligatorio"),
                Objects.requireNonNull(metodoPago, "El método de pago es obligatorio"),
                Instant.now()
        );
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public Instant getOccurredOn() { return occurredOn; }
}
