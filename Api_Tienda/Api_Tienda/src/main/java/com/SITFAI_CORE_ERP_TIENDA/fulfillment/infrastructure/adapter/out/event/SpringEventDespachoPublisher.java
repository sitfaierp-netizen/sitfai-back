package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.OrdenDespachoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringEventDespachoPublisher implements OrdenDespachoEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventDespachoPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publicarTodos(List<DomainEvent> events) {
        events.forEach(this::publicar);
    }
}
