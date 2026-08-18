package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Infraestructura Compartida: GlobalDomainEventAuditListener (AUD-03, MT-01)")
class GlobalDomainEventAuditListenerTest {

    @Mock
    private DomainEventAuditJpaRepository repository;

    private GlobalDomainEventAuditListener listener;

    private final UUID empresaId = UUID.randomUUID();
    private final UUID eventoId = UUID.randomUUID();
    private final UUID pedidoId = UUID.randomUUID();

    // Evento de prueba simulado como Java Record
    record TestPedidoCreadoEvent(
            UUID eventoId,
            UUID pedidoId,
            UUID empresaId,
            String cliente,
            Instant ocurridoEn
    ) {
        public String tipoEvento() {
            return "PEDIDO_CREADO";
        }
    }

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        listener = new GlobalDomainEventAuditListener(repository, objectMapper);
    }

    @Test
    @DisplayName("AUD-03 & MT-01: Debe interceptar evento, serializar a JSON y persistir en Event Store")
    void debeInterceptarYPersistirEvento() {
        Instant ahora = Instant.now();
        TestPedidoCreadoEvent event = new TestPedidoCreadoEvent(
                eventoId,
                pedidoId,
                empresaId,
                "Cliente Juan Perez",
                ahora
        );

        listener.onDomainEvent(event);

        ArgumentCaptor<DomainEventAuditJpaEntity> captor = ArgumentCaptor.forClass(DomainEventAuditJpaEntity.class);
        verify(repository).save(captor.capture());

        DomainEventAuditJpaEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getId()).isEqualTo(eventoId.toString());
        assertThat(savedEntity.getAggregateId()).isEqualTo(pedidoId.toString());
        assertThat(savedEntity.getEventType()).isEqualTo("PEDIDO_CREADO");
        assertThat(savedEntity.getEmpresaId()).isEqualTo(empresaId.toString());
        assertThat(savedEntity.getOccurredOn()).isEqualTo(ahora);
        assertThat(savedEntity.getPayload()).contains("Cliente Juan Perez");
        assertThat(savedEntity.getPayload()).contains(pedidoId.toString());
    }

    @Test
    @DisplayName("Debe ignorar eventos nulos sin lanzar excepción")
    void debeIgnorarEventoNulo() {
        listener.onDomainEvent(null);
        verifyNoInteractions(repository);
    }
}
