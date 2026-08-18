package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Notifica la anulación de una Factura.
 * <p>
 * Inmutable (record Java 25).
 */
public record FacturaAnuladaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        FacturaId facturaId,
        EmpresaId empresaId,
        String motivo
) implements DomainEvent {

    public FacturaAnuladaEvent {
        Objects.requireNonNull(eventoId, "FacturaAnuladaEvent: eventoId es obligatorio.");
        Objects.requireNonNull(ocurridoEn, "FacturaAnuladaEvent: ocurridoEn es obligatorio.");
        Objects.requireNonNull(facturaId, "FacturaAnuladaEvent: facturaId es obligatorio.");
        Objects.requireNonNull(empresaId, "FacturaAnuladaEvent: empresaId es obligatorio (MT-01).");
    }

    public static FacturaAnuladaEvent of(FacturaId facturaId, EmpresaId empresaId, String motivo) {
        return new FacturaAnuladaEvent(
                UUID.randomUUID(),
                Instant.now(),
                facturaId,
                empresaId,
                motivo
        );
    }
}
