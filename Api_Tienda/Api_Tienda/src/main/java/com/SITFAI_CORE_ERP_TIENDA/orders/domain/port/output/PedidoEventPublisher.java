package com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.event.DomainEvent;

public interface PedidoEventPublisher {
    void publish(DomainEvent event);
}
