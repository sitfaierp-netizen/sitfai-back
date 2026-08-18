package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DomainEvent;

import java.util.List;

public interface TurnoCajaEventPublisher {
    void publicar(DomainEvent event);
    void publicarTodos(List<DomainEvent> events);
}
