package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;

public interface PedidoEventPublisher {
    void publicar(DomainEvent event);
}
