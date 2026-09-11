package com.SITFAI_CORE_ERP_TIENDA.pos.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    default String getEventId() { return UUID.randomUUID().toString(); }
    default Instant getOccurredOn() { return Instant.now(); }
}
