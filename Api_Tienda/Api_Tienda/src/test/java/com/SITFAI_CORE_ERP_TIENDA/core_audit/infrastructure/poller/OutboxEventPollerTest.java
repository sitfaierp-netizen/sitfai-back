package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.poller;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosPendientesCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input.ProcesarEventosPendientesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox: OutboxEventPoller Scheduler Tests")
class OutboxEventPollerTest {

    @Mock
    private ProcesarEventosPendientesUseCase useCase;

    private OutboxEventPoller poller;

    @BeforeEach
    void setUp() {
        poller = new OutboxEventPoller(useCase);
    }

    @Test
    @DisplayName("Debe invocar useCase.procesarPendientes periódicamente al dispararse la tarea programada")
    void alEjecutarTareaProgramada_debeInvocarCasoDeUso() {
        when(useCase.procesarPendientes(any(ProcesarEventosPendientesCommand.class)))
                .thenReturn(new ProcesarEventosResponse(5, 0, 5));

        poller.procesarEventosOutbox();

        verify(useCase).procesarPendientes(any(ProcesarEventosPendientesCommand.class));
    }
}
