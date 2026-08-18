package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringBodegaEventPublisherAdapter implements BodegaEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringBodegaEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
