package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.LoteRevertido;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.TicketId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record DevolucionRegistradaEvent(
        UUID eventoId,
        TurnoId turnoId,
        CajaId cajaId,
        EmpresaId empresaId,
        UUID ventaOrigenId, // Agregado para cumplir invariante tributaria (Factura Original)
        List<LineaDevolucion> lineas,
        List<LoteRevertido> lotesRevertidos,
        Dinero montoDevuelto,
        Instant ocurridoEn
) implements DomainEvent {

    public record LineaDevolucion(UUID productoId, int cantidad, java.math.BigDecimal precioUnitario) {}

    public static DevolucionRegistradaEvent of(TurnoId turnoId, CajaId cajaId, EmpresaId empresaId, UUID ventaOrigenId, List<LineaDevolucion> lineas, List<LoteRevertido> lotesRevertidos, Dinero montoDevuelto) {
        return new DevolucionRegistradaEvent(
                UUID.randomUUID(),
                turnoId,
                cajaId,
                empresaId,
                ventaOrigenId,
                List.copyOf(lineas),
                List.copyOf(lotesRevertidos),
                montoDevuelto,
                Instant.now()
        );
    }
    
    @Override
    public String getEventId() { return eventoId.toString(); }

    @Override
    public Instant getOccurredOn() { return ocurridoEn; }
}
