package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventFacturaPublisher implements FacturaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringEventFacturaPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventFacturaPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(DomainEvent event) {
        log.info("Publicando Domain Event desde Billing: {}", event.getClass().getSimpleName());
        applicationEventPublisher.publishEvent(event);
    }
}
