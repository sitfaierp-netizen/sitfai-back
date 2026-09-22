package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosPendientesCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventDispatcherPort;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Outbox: ProcesarEventosPendientesService Application Tests")
class ProcesarEventosPendientesServiceTest {

    @Mock
    private EventStoreRepository repository;

    @Mock
    private EventDispatcherPort dispatcher;

    private ProcesarEventosPendientesService service;

    @BeforeEach
    void setUp() {
        service = new ProcesarEventosPendientesService(repository, dispatcher);
    }

    @Test
    @DisplayName("Debe despachar eventos pendientes exitosamente y transicionar a PROCESADO")
    void alHaberEventosPendientes_debeDespacharYMarcarProcesado() throws Exception {
        StoredDomainEvent evento1 = StoredDomainEvent.notariar(
                StoredEventId.generar(),
                EmpresaId.de(UUID.randomUUID()),
                "FacturaEmitidaEvent",
                Instant.now(),
                "{\"facturaId\":\"123\"}"
        );
        StoredDomainEvent evento2 = StoredDomainEvent.notariar(
                StoredEventId.generar(),
                EmpresaId.de(UUID.randomUUID()),
                "PedidoConfirmadoEvent",
                Instant.now(),
                "{\"pedidoId\":\"456\"}"
        );

        when(repository.buscarPendientes(50)).thenReturn(List.of(evento1, evento2));

        ProcesarEventosResponse response = service.procesarPendientes(new ProcesarEventosPendientesCommand(50));

        assertThat(response.total()).isEqualTo(2);
        assertThat(response.procesados()).isEqualTo(2);
        assertThat(response.fallidos()).isEqualTo(0);

        verify(dispatcher, times(2)).despachar(any(StoredDomainEvent.class));
        verify(repository, times(2)).guardar(any(StoredDomainEvent.class));

        assertThat(evento1.getEstado()).isEqualTo(EventStatus.PROCESADO);
        assertThat(evento2.getEstado()).isEqualTo(EventStatus.PROCESADO);
    }

    @Test
    @DisplayName("Si el despachador falla, debe marcar el evento como FALLIDO con el motivo del error")
    void alFallarDespacho_debeMarcarFallido() throws Exception {
        StoredDomainEvent evento = StoredDomainEvent.notariar(
                StoredEventId.generar(),
                EmpresaId.de(UUID.randomUUID()),
                "StockReservadoEvent",
                Instant.now(),
                "{\"productoId\":\"abc\"}"
        );

        when(repository.buscarPendientes(10)).thenReturn(List.of(evento));
        doThrow(new RuntimeException("Error de timeout en broker")).when(dispatcher).despachar(evento);

        ProcesarEventosResponse response = service.procesarPendientes(new ProcesarEventosPendientesCommand(10));

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.procesados()).isEqualTo(0);
        assertThat(response.fallidos()).isEqualTo(1);

        verify(repository).guardar(evento);
        assertThat(evento.getEstado()).isEqualTo(EventStatus.FALLIDO);
        assertThat(evento.getMotivoFallo()).contains("Error de timeout en broker");
    }

    @Test
    @DisplayName("Si no hay eventos pendientes, retorna respuesta vacía de inmediato")
    void alNoHaberEventos_retornaVacio() {
        when(repository.buscarPendientes(50)).thenReturn(Collections.emptyList());

        ProcesarEventosResponse response = service.procesarPendientes(new ProcesarEventosPendientesCommand(50));

        assertThat(response.total()).isZero();
        assertThat(response.procesados()).isZero();
        assertThat(response.fallidos()).isZero();
        verifyNoInteractions(dispatcher);
    }
}
