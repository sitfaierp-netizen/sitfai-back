package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.RolCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.port.out.RolEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RolEventPublisherAdapter implements RolEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RolEventPublisherAdapter.class);
    private final ApplicationEventPublisher eventPublisher;

    public RolEventPublisherAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publicar(RolCreadoEvent event) {
        log.info("Publicando RolCreadoEvent para rol: {} - {}", event.getCodigo(), event.getNombre());
        eventPublisher.publishEvent(event);
    }
}
