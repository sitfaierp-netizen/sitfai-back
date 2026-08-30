package com.SITFAI_CORE_ERP_TIENDA.orders.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    Instant occurredOn();
}
