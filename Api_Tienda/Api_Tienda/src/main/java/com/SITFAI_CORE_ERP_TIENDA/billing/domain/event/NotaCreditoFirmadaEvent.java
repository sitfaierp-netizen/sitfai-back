package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Cufe;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.NotaCreditoId;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: Emitido cuando una nota de crédito se firma electrónicamente.
 */
public record NotaCreditoFirmadaEvent(
        UUID eventoId,
        NotaCreditoId notaCreditoId,
        Cufe cufeAsignado,
        Instant ocurridoEn
) implements DomainEvent {
    public static NotaCreditoFirmadaEvent of(NotaCreditoId notaCreditoId, Cufe cufeAsignado) {
        return new NotaCreditoFirmadaEvent(UUID.randomUUID(), notaCreditoId, cufeAsignado, Instant.now());
    }
}
