package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.PedidoOrigenId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Ruc;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Notifica la emisión exitosa de una Factura de venta.
 * <p>
 * Inmutable (record Java 25).
 */
public record FacturaEmitidaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        FacturaId facturaId,
        EmpresaId empresaId,
        PedidoOrigenId pedidoOrigenId,
        Ruc rucCliente,
        Dinero total
) implements DomainEvent {

    public FacturaEmitidaEvent {
        Objects.requireNonNull(eventoId, "FacturaEmitidaEvent: eventoId es obligatorio.");
        Objects.requireNonNull(ocurridoEn, "FacturaEmitidaEvent: ocurridoEn es obligatorio.");
        Objects.requireNonNull(facturaId, "FacturaEmitidaEvent: facturaId es obligatorio.");
        Objects.requireNonNull(empresaId, "FacturaEmitidaEvent: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(pedidoOrigenId, "FacturaEmitidaEvent: pedidoOrigenId es obligatorio.");
        Objects.requireNonNull(total, "FacturaEmitidaEvent: total es obligatorio.");
    }

    public static FacturaEmitidaEvent of(
            FacturaId facturaId,
            EmpresaId empresaId,
            PedidoOrigenId pedidoOrigenId,
            Ruc rucCliente,
            Dinero total) {
        return new FacturaEmitidaEvent(
                UUID.randomUUID(),
                Instant.now(),
                facturaId,
                empresaId,
                pedidoOrigenId,
                rucCliente,
                total
        );
    }

    public static FacturaEmitidaEvent of(
            FacturaId facturaId,
            EmpresaId empresaId,
            PedidoOrigenId pedidoOrigenId,
            Dinero total) {
        return new FacturaEmitidaEvent(
                UUID.randomUUID(),
                Instant.now(),
                facturaId,
                empresaId,
                pedidoOrigenId,
                null,
                total
        );
    }
}
