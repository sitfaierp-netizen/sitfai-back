package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.messaging;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventDispatcherPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Driven Adapter: Despachador de Eventos Outbox (AUD-03).
 * <p>
 * Satisface el puerto {@link EventDispatcherPort} utilizando el {@link ApplicationEventPublisher}
 * de Spring para reenviar el payload deserializado hacia el bus de eventos de la aplicación,
 * imitando la coreografía y comunicación inter-módulos.
 */
@Component
public class EventDispatcherAdapter implements EventDispatcherPort {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcherAdapter.class);

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public EventDispatcherAdapter(ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher no puede ser null");
        this.objectMapper = objectMapper != null
                ? objectMapper.rebuild().findAndAddModules().build()
                : JsonMapper.builder().findAndAddModules().build();
    }

    @Override
    public void despachar(StoredDomainEvent evento) throws Exception {
        Objects.requireNonNull(evento, "evento no puede ser null");

        log.info("Despachando evento Outbox: id={}, nombreEvento={}, empresaId={}",
                evento.getId(), evento.getNombreEvento(), evento.getEmpresaId());

        JsonNode jsonNode = objectMapper.readTree(evento.getPayload());

        OutboxMessageEvent mensajeOutbox = new OutboxMessageEvent(
                evento.getId().valor(),
                evento.getEmpresaId().valor(),
                evento.getNombreEvento(),
                evento.getOcurridoEn(),
                evento.getPayload(),
                jsonNode
        );

        eventPublisher.publishEvent(mensajeOutbox);
        log.debug("Evento Outbox {} publicado exitosamente en el bus de eventos.", evento.getId());
    }
}
