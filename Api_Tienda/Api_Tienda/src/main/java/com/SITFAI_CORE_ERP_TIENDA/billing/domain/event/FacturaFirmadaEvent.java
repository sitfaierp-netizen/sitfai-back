package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Cufe;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Emitido cuando una factura electrónica se firma con su CUFE.
 */
public record FacturaFirmadaEvent(
        UUID eventoId,
        FacturaId facturaId,
        Cufe cufe,
        Instant ocurridoEn
) implements DomainEvent {
    public static FacturaFirmadaEvent of(FacturaId facturaId, Cufe cufe) {
        return new FacturaFirmadaEvent(UUID.randomUUID(), facturaId, cufe, Instant.now());
    }
}
