package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.NotariarEventoCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auditoría: NotariarEventoService Application Tests")
class NotariarEventoServiceTest {

    @Mock
    private EventStoreRepository repository;

    private ObjectMapper objectMapper;
    private NotariarEventoService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new NotariarEventoService(repository, objectMapper);
    }

    @Test
    @DisplayName("Debe serializar evento original y persistir StoredDomainEvent en estado PENDIENTE")
    void alNotariarConEventoOriginal_debeSerializarYPersistir() {
        UUID empresaId = UUID.randomUUID();
        Instant ahora = Instant.now();
        Map<String, Object> eventoData = Map.of(
                "facturaId", UUID.randomUUID().toString(),
                "monto", "150000.00"
        );

        NotariarEventoCommand command = NotariarEventoCommand.desdeEvento(
                empresaId,
                "FacturaEmitidaEvent",
                ahora,
                eventoData
        );

        when(repository.guardar(any(StoredDomainEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoredDomainEvent resultado = service.notariarEvento(command);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmpresaId().valor()).isEqualTo(empresaId);
        assertThat(resultado.getNombreEvento()).isEqualTo("FacturaEmitidaEvent");
        assertThat(resultado.getEstado()).isEqualTo(EventStatus.PENDIENTE);
        assertThat(resultado.getPayload()).contains("150000.00");

        ArgumentCaptor<StoredDomainEvent> captor = ArgumentCaptor.forClass(StoredDomainEvent.class);
        verify(repository).guardar(captor.capture());
        assertThat(captor.getValue().getPayload()).contains("facturaId");
    }

    @Test
    @DisplayName("Debe usar payloadJson directamente si ya fue suministrado en el comando")
    void alNotariarConPayloadJson_debePersistirDirectamente() {
        UUID empresaId = UUID.randomUUID();
        String json = "{\"mensaje\":\"OK\"}";

        NotariarEventoCommand command = NotariarEventoCommand.conPayload(
                null,
                empresaId,
                "TestEvent",
                Instant.now(),
                json
        );

        when(repository.guardar(any(StoredDomainEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoredDomainEvent resultado = service.notariarEvento(command);

        assertThat(resultado.getPayload()).isEqualTo(json);
        verify(repository).guardar(any(StoredDomainEvent.class));
    }

    @Test
    @DisplayName("Debe fallar si no se provee ni payload ni eventoOriginal")
    void alNotariarSinDatos_debeLanzarExcepcion() {
        UUID empresaId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                new NotariarEventoCommand(null, empresaId, "TestEvent", Instant.now(), null, null)
        );
    }
}
