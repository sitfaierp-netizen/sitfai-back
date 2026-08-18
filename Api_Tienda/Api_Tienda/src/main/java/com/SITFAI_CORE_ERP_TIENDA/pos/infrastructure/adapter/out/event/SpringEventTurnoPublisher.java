package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringEventTurnoPublisher implements TurnoCajaEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventTurnoPublisher(ApplicationEventPublisher applicationEventPublisher) {
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
