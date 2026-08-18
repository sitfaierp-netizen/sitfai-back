package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.messaging;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.SourcingEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Driven Adapter: Implementa SourcingEventPublisherPort usando la infraestructura de eventos de Spring.
 */
@Component
public class SourcingSpringEventPublisherAdapter implements SourcingEventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SourcingSpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    }

    @Override
    public void publicar(DomainEvent evento) {
        applicationEventPublisher.publishEvent(evento);
    }
}
