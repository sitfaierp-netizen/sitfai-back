package com.SITFAI_CORE_ERP_TIENDA.core_audit;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import org.springframework.context.annotation.Import;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosPendientesCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input.ProcesarEventosPendientesUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.entity.StoredEventJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.repository.StoredEventJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.poller.OutboxEventPoller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de Integración: Outbox Dispatcher (Poller) y Despacho Asíncrono (AUD-03, MT-01).
 * <p>
 * Certifica que:
 * 1. El motor recupera un evento simulado en estado PENDIENTE en base de datos.
 * 2. Lo despacha vía EventDispatcherPort hacia los suscriptores.
 * 3. Actualiza y sella su estado a PROCESADO en la tabla {@code core_audit_event_store}.
 */
class OutboxIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Autowired
    private StoredEventJpaRepository jpaRepository;

    @Autowired
    private ProcesarEventosPendientesUseCase useCase;

    @Autowired
    private OutboxEventPoller outboxEventPoller;

    @Test
    @DisplayName("Debe recuperar un evento PENDIENTE, despacharlo y marcarlo como PROCESADO")
    void alHaberEventoPendiente_debeDespacharYTransicionarAProcesado() {
        // 1. ARRANGE: Simular evento en estado PENDIENTE guardado en Event Store
        UUID empresaId = UUID.randomUUID();
        StoredEventId eventId = StoredEventId.generar();
        String jsonPayload = "{\"tipo\":\"INTEGRACION\",\"total\":75000.00}";

        StoredDomainEvent eventoPendiente = StoredDomainEvent.notariar(
                eventId,
                EmpresaId.de(empresaId),
                "PedidoConfirmadoEvent",
                Instant.now(),
                jsonPayload
        );
        eventStoreRepository.guardar(eventoPendiente);

        // Verificar precondición en BD
        Optional<StoredEventJpaEntity> guardadoAntes = jpaRepository.findByIdAndEmpresaId(eventId.valor(), empresaId);
        assertThat(guardadoAntes).isPresent();
        assertThat(guardadoAntes.get().getEstado()).isEqualTo(EventStatus.PENDIENTE);

        // 2. ACT: Ejecutar procesamiento de lote Outbox (invocando el caso de uso del Poller)
        ProcesarEventosResponse response = useCase.procesarPendientes(new ProcesarEventosPendientesCommand(10));

        // 3. ASSERT: Validar métricas de procesamiento
        assertThat(response.procesados()).isGreaterThanOrEqualTo(1);

        // Validar postcondición en BD viva: estado PROCESADO y procesadoEn no nulo
        Optional<StoredEventJpaEntity> guardadoDespues = jpaRepository.findByIdAndEmpresaId(eventId.valor(), empresaId);
        assertThat(guardadoDespues).isPresent();
        assertThat(guardadoDespues.get().getEstado()).isEqualTo(EventStatus.PROCESADO);
        assertThat(guardadoDespues.get().getProcesadoEn()).isNotNull();
        assertThat(guardadoDespues.get().getMotivoFallo()).isNull();
    }
}
