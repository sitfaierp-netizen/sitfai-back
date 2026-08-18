package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.DomainEvent;

import java.util.List;

public interface EmpresaEventPublisher {
    void publicar(DomainEvent event);
    void publicarTodos(List<DomainEvent> events);
}
