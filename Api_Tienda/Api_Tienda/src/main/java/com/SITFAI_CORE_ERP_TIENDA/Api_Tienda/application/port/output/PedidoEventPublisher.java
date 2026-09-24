package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import java.util.List;

public interface PedidoEventPublisher {
    void publicar(DomainEvent event);

    default void publicarTodos(List<? extends DomainEvent> events) {
        if (events != null) {
            events.forEach(this::publicar);
        }
    }
}
