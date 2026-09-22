package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosPendientesCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input.ProcesarEventosPendientesUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventDispatcherPort;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de Aplicación: Orquestador del despacho de eventos Outbox pendientes (AUD-03).
 * <p>
 * Recupera un lote de eventos en estado PENDIENTE, los despacha hacia el bus de integración
 * mediante {@link EventDispatcherPort} y actualiza el estado en el Event Store:
 * <ul>
 *   <li>Si el despacho es exitoso: {@link StoredDomainEvent#marcarProcesado()}.</li>
 *   <li>Si el despacho falla: {@link StoredDomainEvent#marcarFallido(String)}.</li>
 * </ul>
 */
@Service
public class ProcesarEventosPendientesService implements ProcesarEventosPendientesUseCase, com.SITFAI_CORE_ERP_TIENDA.core_audit.application.usecase.ProcesarEventosPendientesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcesarEventosPendientesService.class);

    private final EventStoreRepository repository;
    private final EventDispatcherPort dispatcher;

    public ProcesarEventosPendientesService(EventStoreRepository repository, EventDispatcherPort dispatcher) {
        this.repository = Objects.requireNonNull(repository, "repository no puede ser null");
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher no puede ser null");
    }

    @Override
    @Transactional
    public ProcesarEventosResponse procesarPendientes(ProcesarEventosPendientesCommand command) {
        int batchSize = (command != null && command.batchSize() > 0) ? command.batchSize() : 50;

        List<StoredDomainEvent> pendientes = repository.buscarPendientes(batchSize);
        if (pendientes.isEmpty()) {
            return ProcesarEventosResponse.vacio();
        }

        log.debug("Iniciando despacho Outbox de {} eventos pendientes...", pendientes.size());

        int procesados = 0;
        int fallidos = 0;

        for (StoredDomainEvent evento : pendientes) {
            try {
                dispatcher.despachar(evento);
                evento.marcarProcesado();
                repository.guardar(evento);
                procesados++;
                log.debug("Evento Outbox {} despachado y marcado como PROCESADO", evento.getId());
            } catch (Exception e) {
                fallidos++;
                String motivo = e.getMessage() != null && !e.getMessage().isBlank()
                        ? e.getMessage()
                        : e.getClass().getSimpleName();
                log.error("Fallo al despachar evento Outbox id={} tipo={}: {}",
                        evento.getId(), evento.getNombreEvento(), motivo, e);
                try {
                    evento.marcarFallido(motivo);
                    repository.guardar(evento);
                } catch (Exception persistenceEx) {
                    log.error("Error crítico al actualizar estado FALLIDO del evento {}: {}",
                            evento.getId(), persistenceEx.getMessage(), persistenceEx);
                }
            }
        }

        log.info("Lote Outbox finalizado: total={}, procesados={}, fallidos={}",
                pendientes.size(), procesados, fallidos);

        return new ProcesarEventosResponse(procesados, fallidos, pendientes.size());
    }
}
