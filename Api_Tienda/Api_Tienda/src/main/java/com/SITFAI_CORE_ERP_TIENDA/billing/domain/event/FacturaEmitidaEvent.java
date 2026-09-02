package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FacturaEmitidaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        UUID facturaId,
        UUID empresaId,
        BigDecimal totalFacturado
) implements DomainEvent {
    
    public FacturaEmitidaEvent(UUID facturaId, UUID empresaId, BigDecimal totalFacturado) {
        this(UUID.randomUUID(), Instant.now(), facturaId, empresaId, totalFacturado);
    }
}
