package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Adaptador de Eventos: Implementación del puerto {@link PedidoEventPublisher}.
 * <p>
 * Publica los Domain Events internamente a través del {@link ApplicationEventPublisher} de Spring (AUD-03).
 * Puede ser reemplazado o extendido en el futuro por un adaptador a Apache Kafka sin afectar la Capa de Aplicación.
 */
@Component
public class SpringEventPedidoPublisher implements PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringEventPedidoPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPedidoPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher, "applicationEventPublisher es requerido.");
    }

    @Override
    public void publicar(DomainEvent evento) {
        Objects.requireNonNull(evento, "SpringEventPedidoPublisher: evento no puede ser null.");
        log.info("[DOMAIN EVENT] Publicando evento: {} ocurrido en: {}",
                evento.getClass().getSimpleName(), evento.ocurridoEn());

        applicationEventPublisher.publishEvent(evento);
    }
}
