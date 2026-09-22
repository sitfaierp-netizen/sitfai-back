package com.SITFAI_CORE_ERP_TIENDA.core_audit;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.entity.StoredEventJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.repository.StoredEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de Integración: Valida el flujo completo de persistencia del Event Store (AUD-03, MT-01).
 * <p>
 * Simula la emisión de un evento de negocio (ej. {@link FacturaEmitidaEvent}) y verifica que:
 * 1. El listener global lo intercepta en la fase BEFORE_COMMIT (patrón Outbox).
 * 2. Se almacena permanentemente en la tabla {@code core_audit_event_store}.
 * 3. El payload contiene la serialización JSON del evento.
 * 4. El aislamiento multi-tenant por {@code empresa_id} se preserva estrictamente.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class EventStoreIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("sitfai_tienda")
            .withUsername("sitfai_user")
            .withPassword("sitfai_secret_pwd");

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private StoredEventJpaRepository jpaRepository;

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("Debe capturar FacturaEmitidaEvent y persistirlo en core_audit_event_store con su JSON y MT-01")
    void alEmitirEventoDeDominio_debePersistirEnEventStoreConPayloadJson() {
        // ARRANGE
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        BigDecimal total = new BigDecimal("250000.0000");

        FacturaEmitidaEvent evento = new FacturaEmitidaEvent(facturaId, empresaId, total);

        // ACT: Publicar evento dentro de un límite transaccional para activar BEFORE_COMMIT
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(evento);
        });

        // ASSERT: Verificación directa en base de datos viva (Caja Negra)
        List<StoredEventJpaEntity> eventos = jpaRepository.findByEmpresaIdAndNombreEventoOrderByOcurridoEnDesc(
                empresaId, "FacturaEmitidaEvent"
        );

        assertThat(eventos).isNotEmpty();
        StoredEventJpaEntity entity = eventos.getFirst();

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getEmpresaId()).isEqualTo(empresaId);
        assertThat(entity.getNombreEvento()).isEqualTo("FacturaEmitidaEvent");
        assertThat(entity.getEstado()).isEqualTo(EventStatus.PENDIENTE);
        assertThat(entity.getPayload()).contains(facturaId.toString());
        assertThat(entity.getPayload()).contains(empresaId.toString());
        assertThat(entity.getPayload()).contains("250000");
        assertThat(entity.getCreadoEn()).isNotNull();
        assertThat(entity.getCreadoPor()).isNotNull();

        // ASSERT: Verificación a través del Driven Port de Dominio
        List<StoredDomainEvent> eventosDominio = eventStoreRepository.buscarPorEmpresa(EmpresaId.de(empresaId));
        assertThat(eventosDominio).isNotEmpty();
        StoredDomainEvent eventoDominio = eventosDominio.getFirst();
        assertThat(eventoDominio.getEmpresaId().valor()).isEqualTo(empresaId);
        assertThat(eventoDominio.getNombreEvento()).isEqualTo("FacturaEmitidaEvent");
        assertThat(eventoDominio.getEstado()).isEqualTo(EventStatus.PENDIENTE);
    }
}
