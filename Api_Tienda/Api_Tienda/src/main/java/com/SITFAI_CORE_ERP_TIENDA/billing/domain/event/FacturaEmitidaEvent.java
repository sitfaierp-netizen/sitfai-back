package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio emitido cuando una Factura es emitida exitosamente.
 * <p>
 * Reglas validadas: REGLA-3 (Domain Events inmutables), MT-01 (Aislamiento Multitenant), AUD-04.
 */
public record FacturaEmitidaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        UUID facturaId,
        UUID empresaId,
        String documentoFuenteTipo,
        String documentoFuenteNumero,
        UUID clienteId,
        BigDecimal totalFacturado
) implements DomainEvent {

    public FacturaEmitidaEvent {
        Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
        Objects.requireNonNull(facturaId, "facturaId es obligatorio");
        Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01)");
        Objects.requireNonNull(totalFacturado, "totalFacturado es obligatorio");
    }

    public FacturaEmitidaEvent(UUID facturaId, UUID empresaId, BigDecimal totalFacturado) {
        this(UUID.randomUUID(), Instant.now(), facturaId, empresaId, "DOCUMENTO", facturaId.toString(), null, totalFacturado);
    }

    public static FacturaEmitidaEvent of(FacturaId facturaId, EmpresaId empresaId, DocumentoFuenteId documentoFuenteId, ClienteId clienteId, Dinero total) {
        return new FacturaEmitidaEvent(
                UUID.randomUUID(),
                Instant.now(),
                facturaId.valor(),
                empresaId.valor(),
                documentoFuenteId.tipo(),
                documentoFuenteId.numero(),
                clienteId != null ? clienteId.valor() : null,
                total.monto()
        );
    }
}
