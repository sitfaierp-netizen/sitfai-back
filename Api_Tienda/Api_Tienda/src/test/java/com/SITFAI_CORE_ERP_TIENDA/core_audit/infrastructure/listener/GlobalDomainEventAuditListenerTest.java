package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.listener;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.NotariarEventoCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input.NotariarEventoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auditoría: GlobalDomainEventAuditListener Infrastructure Tests")
class GlobalDomainEventAuditListenerTest {

    @Mock
    private NotariarEventoUseCase useCase;

    private GlobalDomainEventAuditListener listener;

    @BeforeEach
    void setUp() {
        listener = new GlobalDomainEventAuditListener(useCase);
    }

    @Test
    @DisplayName("Debe interceptar un FacturaEmitidaEvent, extraer empresaId y enviarlo al caso de uso")
    void alRecibirFacturaEmitidaEvent_debeDespacharAlUseCase() {
        UUID empresaId = UUID.randomUUID();
        UUID facturaId = UUID.randomUUID();
        FacturaEmitidaEvent evento = new FacturaEmitidaEvent(facturaId, empresaId, new BigDecimal("1000.00"));

        listener.onDomainEvent(evento);

        ArgumentCaptor<NotariarEventoCommand> captor = ArgumentCaptor.forClass(NotariarEventoCommand.class);
        verify(useCase).notariarEvento(captor.capture());

        NotariarEventoCommand command = captor.getValue();
        assertThat(command.empresaId()).isEqualTo(empresaId);
        assertThat(command.nombreEvento()).isEqualTo("FacturaEmitidaEvent");
        assertThat(command.eventoOriginal()).isEqualTo(evento);
    }

    @Test
    @DisplayName("Debe ignorar eventos de framework de Spring")
    void alRecibirEventoDeFramework_debeIgnorar() {
        org.springframework.context.PayloadApplicationEvent<String> springEvent =
                new org.springframework.context.PayloadApplicationEvent<>(this, "payload");

        listener.onDomainEvent(springEvent);

        verify(useCase, never()).notariarEvento(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Debe ignorar objetos nulos o no catalogados como DomainEvent")
    void alRecibirObjetoNoEvento_debeIgnorar() {
        listener.onDomainEvent(null);
        listener.onDomainEvent("Un string simple");

        verify(useCase, never()).notariarEvento(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Debe extraer empresaId genéricamente desde un record con campo empresaId")
    void testExtractEmpresaIdGenerico() {
        UUID empresaId = UUID.randomUUID();
        record CustomTestEvent(UUID empresaId, String dato) {}

        CustomTestEvent customEvent = new CustomTestEvent(empresaId, "test");
        UUID extracted = listener.extractEmpresaId(customEvent);

        assertThat(extracted).isEqualTo(empresaId);
    }
}
