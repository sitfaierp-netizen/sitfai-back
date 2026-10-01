package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.messaging;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox: EventDispatcherAdapter Infrastructure Tests")
class EventDispatcherAdapterTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private EventDispatcherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EventDispatcherAdapter(eventPublisher, JsonMapper.builder().findAndAddModules().build());
    }

    @Test
    @DisplayName("Debe deserializar payload y publicar OutboxMessageEvent al bus de Spring")
    void alDespacharEvento_debePublicarOutboxMessageEvent() throws Exception {
        UUID empresaId = UUID.randomUUID();
        StoredEventId eventId = StoredEventId.generar();
        String json = "{\"codigo\":\"ORD-999\",\"total\":3500.00}";

        StoredDomainEvent evento = StoredDomainEvent.notariar(
                eventId,
                EmpresaId.de(empresaId),
                "OrdenCompraEmitidaEvent",
                Instant.now(),
                json
        );

        adapter.despachar(evento);

        ArgumentCaptor<OutboxMessageEvent> captor = ArgumentCaptor.forClass(OutboxMessageEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        OutboxMessageEvent mensaje = captor.getValue();
        assertThat(mensaje.id()).isEqualTo(eventId.valor());
        assertThat(mensaje.empresaId()).isEqualTo(empresaId);
        assertThat(mensaje.nombreEvento()).isEqualTo("OrdenCompraEmitidaEvent");
        assertThat(mensaje.payloadJson()).isEqualTo(json);
        assertThat(mensaje.payload().get("codigo").asText()).isEqualTo("ORD-999");
        assertThat(mensaje.payload().get("total").asDouble()).isEqualTo(3500.00);
    }
}
