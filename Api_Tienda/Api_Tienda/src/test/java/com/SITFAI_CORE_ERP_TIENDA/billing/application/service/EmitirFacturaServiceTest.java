package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmitirFacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaEventPublisher eventPublisher;

    @Mock
    private ActorProviderPort actorProvider;

    private EmitirFacturaService emitirFacturaService;

    private UUID empresaId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        emitirFacturaService = new EmitirFacturaService(facturaRepository, actorProvider, eventPublisher);
    }

    @Test
    void dadoComandoValido_cuandoEmitirFactura_entoncesGuardaYRetornaFactura() {
        // Arrange
        when(actorProvider.getCurrentActorId()).thenReturn("user-test");

        EmitirFacturaCommand.ImpuestoCommand impuestoIva =
                new EmitirFacturaCommand.ImpuestoCommand("IVA", new BigDecimal("19"));
        EmitirFacturaCommand.LineaFacturaCommand linea =
                new EmitirFacturaCommand.LineaFacturaCommand(
                        "Consultoría", new BigDecimal("1"), new BigDecimal("100000"), "COP",
                        List.of(impuestoIva));

        EmitirFacturaCommand command = new EmitirFacturaCommand(
                empresaId, UUID.randomUUID(), null, "0912345678001", List.of(linea)
        );

        // Act
        FacturaResponse response = emitirFacturaService.emitirFactura(command);

        // Assert
        assertNotNull(response);
        assertEquals("EMITIDO", response.estado());
        assertNotNull(response.id());

        verify(facturaRepository).save(any());
        verify(eventPublisher).publicar(any());
    }

    @Test
    void dadoComandoSinEmpresa_cuandoEmitirFactura_entoncesLanzaExcepcionMT01() {
        when(actorProvider.getCurrentActorId()).thenReturn("user-test");

        EmitirFacturaCommand command = new EmitirFacturaCommand(
                null, UUID.randomUUID(), null, "0912345678001", List.of()
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                emitirFacturaService.emitirFactura(command));

        assertTrue(exception.getMessage().contains("EmpresaId es obligatorio"));
        verifyNoInteractions(facturaRepository);
    }
}
