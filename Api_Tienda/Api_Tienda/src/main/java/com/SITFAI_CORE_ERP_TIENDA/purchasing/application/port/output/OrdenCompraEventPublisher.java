package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;

public interface OrdenCompraEventPublisher {
    void publicar(DomainEvent event);
}
