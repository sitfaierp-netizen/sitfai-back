package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.OrdenCompraEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventOrdenCompraPublisher implements OrdenCompraEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringEventOrdenCompraPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventOrdenCompraPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(DomainEvent event) {
        log.info("Publicando Domain Event desde Purchasing: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
