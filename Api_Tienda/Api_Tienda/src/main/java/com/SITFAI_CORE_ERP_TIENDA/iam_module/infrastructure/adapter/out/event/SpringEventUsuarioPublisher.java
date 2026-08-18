package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output.UsuarioEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Driven Adapter: Publicador de eventos de dominio IAM utilizando el ApplicationEventPublisher de Spring.
 */
@Component
public class SpringEventUsuarioPublisher implements UsuarioEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public SpringEventUsuarioPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher no puede ser null.");
    }

    @Override
    public void publicar(DomainEvent evento) {
        if (evento != null) {
            eventPublisher.publishEvent(evento);
        }
    }

    @Override
    public void publicarTodos(List<DomainEvent> eventos) {
        if (eventos != null) {
            eventos.forEach(this::publicar);
        }
    }
}
