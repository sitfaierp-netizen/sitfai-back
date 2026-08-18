package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.event.DomainEvent;

import java.util.List;

public interface OrdenDespachoEventPublisher {
    void publicar(DomainEvent event);
    void publicarTodos(List<DomainEvent> events);
}
