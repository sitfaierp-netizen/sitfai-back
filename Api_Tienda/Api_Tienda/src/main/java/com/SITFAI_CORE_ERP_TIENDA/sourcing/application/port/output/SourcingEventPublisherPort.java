package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.event.DomainEvent;

/** Driven Port para publicar Domain Events. */
public interface SourcingEventPublisherPort {
    void publicar(DomainEvent evento);
}
