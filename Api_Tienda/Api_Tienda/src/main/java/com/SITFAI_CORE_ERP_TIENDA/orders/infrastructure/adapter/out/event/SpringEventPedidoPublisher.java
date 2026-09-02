package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output.PedidoEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("ordersSpringEventPedidoPublisher")
public class SpringEventPedidoPublisher implements PedidoEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPedidoPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
