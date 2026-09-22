package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.NotariarEventoCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input.NotariarEventoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * Servicio de Aplicación: Orquestador de la notarización de eventos en el Event Store (AUD-03, MT-01).
 * <p>
 * Responsabilidad única:
 * <ol>
 *   <li>Validar y estructurar el comando de entrada.</li>
 *   <li>Serializar de forma segura y encapsulada el payload del evento original a JSON vía Jackson.</li>
 *   <li>Instanciar el Aggregate Root {@link StoredDomainEvent} mediante su factory method {@code notariar(...)}.</li>
 *   <li>Persistir el registro inmutable a través del Driven Port {@link EventStoreRepository}.</li>
 * </ol>
 */
@Service
public class NotariarEventoService implements NotariarEventoUseCase, com.SITFAI_CORE_ERP_TIENDA.core_audit.application.usecase.NotariarEventoUseCase {

    private static final Logger log = LoggerFactory.getLogger(NotariarEventoService.class);

    private final EventStoreRepository repository;
    private final ObjectMapper objectMapper;

    public NotariarEventoService(EventStoreRepository repository, ObjectMapper objectMapper) {
        this.repository = Objects.requireNonNull(repository, "NotariarEventoService: repository no puede ser null.");
        this.objectMapper = objectMapper != null
                ? objectMapper.copy().registerModule(new JavaTimeModule())
                : new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    @Transactional
    public StoredDomainEvent notariarEvento(NotariarEventoCommand command) {
        Objects.requireNonNull(command, "NotariarEventoService: command no puede ser null.");

        StoredEventId eventId = command.id() != null ? command.id() : StoredEventId.generar();
        EmpresaId empresaId = EmpresaId.de(command.empresaId());
        Instant ocurridoEn = command.ocurridoEn() != null ? command.ocurridoEn() : Instant.now();

        String payloadJson = resolverPayloadJson(command);

        StoredDomainEvent evento = StoredDomainEvent.notariar(
                eventId,
                empresaId,
                command.nombreEvento(),
                ocurridoEn,
                payloadJson
        );

        StoredDomainEvent guardado = repository.guardar(evento);
        log.debug("Evento notariado exitosamente en Event Store: id={}, evento={}, empresaId={}",
                guardado.getId(), guardado.getNombreEvento(), guardado.getEmpresaId());

        return guardado;
    }

    private String resolverPayloadJson(NotariarEventoCommand command) {
        if (command.payloadJson() != null && !command.payloadJson().isBlank()) {
            return command.payloadJson().trim();
        }

        if (command.eventoOriginal() != null) {
            try {
                return objectMapper.writeValueAsString(command.eventoOriginal());
            } catch (JsonProcessingException e) {
                log.error("Error al serializar el evento [{}] a JSON: {}",
                        command.nombreEvento(), e.getMessage(), e);
                throw new IllegalArgumentException("No fue posible serializar el payload del evento a JSON", e);
            }
        }

        throw new IllegalArgumentException("NotariarEventoCommand: No se especificó payload ni eventoOriginal para serializar.");
    }
}
