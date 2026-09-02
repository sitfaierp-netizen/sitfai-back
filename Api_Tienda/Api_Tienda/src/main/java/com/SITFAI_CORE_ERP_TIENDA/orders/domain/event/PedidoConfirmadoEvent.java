package com.SITFAI_CORE_ERP_TIENDA.orders.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;

import java.time.Instant;
import java.util.UUID;

public record PedidoConfirmadoEvent(
        UUID eventId,
        Instant occurredOn,
        PedidoId pedidoId,
        UUID empresaId
) implements DomainEvent {
    
    public PedidoConfirmadoEvent(PedidoId pedidoId, UUID empresaId) {
        this(UUID.randomUUID(), Instant.now(), pedidoId, empresaId);
    }
}
