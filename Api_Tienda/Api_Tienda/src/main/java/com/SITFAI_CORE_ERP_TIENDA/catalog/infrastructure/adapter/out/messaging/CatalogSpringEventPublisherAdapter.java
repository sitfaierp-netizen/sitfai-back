package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.messaging;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.CatalogEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Driven Adapter: Implementa CatalogEventPublisherPort usando la infraestructura de eventos de Spring.
 * Permite la coreografía intra-proceso y emisión de eventos asíncronos si está habilitado.
 */
@Component
public class CatalogSpringEventPublisherAdapter implements CatalogEventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public CatalogSpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
    }

    @Override
    public void publicar(DomainEvent evento) {
        applicationEventPublisher.publishEvent(evento);
    }
}
