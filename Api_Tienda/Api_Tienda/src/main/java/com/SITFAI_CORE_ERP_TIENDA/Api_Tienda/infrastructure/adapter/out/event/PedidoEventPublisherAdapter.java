package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;

import lombok.RequiredArgsConstructor;

@Component("apiTiendaPedidoEventPublisherAdapter")
@RequiredArgsConstructor
public class PedidoEventPublisherAdapter implements PedidoEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publicar(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
