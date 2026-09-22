package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.port.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
    private ApplicationEventPublisher eventPublisher;

    private EmitirFacturaService emitirFacturaService;

    private UUID empresaId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        emitirFacturaService = new EmitirFacturaService(facturaRepository, eventPublisher);
    }

    @Test
    void dadoComandoValido_cuandoEmitirFactura_entoncesGuardaYRetornaFactura() {
        // Arrange
        when(facturaRepository.guardar(any(Factura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmitirFacturaCommand.ImpuestoCommand impuestoIva =
                new EmitirFacturaCommand.ImpuestoCommand("IVA", new BigDecimal("19"));
        EmitirFacturaCommand.LineaFacturaCommand linea =
                new EmitirFacturaCommand.LineaFacturaCommand(
                        "Consultoría", new BigDecimal("1"), new BigDecimal("100000"), "COP",
                        List.of(impuestoIva));

        UUID pedidoId = UUID.randomUUID();
        EmitirFacturaCommand command = new EmitirFacturaCommand(
                empresaId, UUID.randomUUID(), pedidoId, "0912345678001", List.of(linea)
        );

        // Act
        FacturaResponse response = emitirFacturaService.emitirFactura(command);

        // Assert
        assertNotNull(response);
        assertEquals("EMITIDA", response.estado());
        assertNotNull(response.id());
        assertEquals(empresaId.toString(), response.empresaId());
        assertEquals(pedidoId.toString(), response.pedidoId());

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository).guardar(captor.capture());
        Factura facturaGuardada = captor.getValue();
        assertEquals(empresaId, facturaGuardada.getEmpresaId().valor());

        verify(eventPublisher, atLeastOnce()).publishEvent(any(com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaEmitidaEvent.class));
    }

    @Test
    void dadoComandoSinEmpresa_cuandoEmitirFactura_entoncesLanzaExcepcionMT01() {
        EmitirFacturaCommand command = new EmitirFacturaCommand(
                null, UUID.randomUUID(), null, "0912345678001", List.of()
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                emitirFacturaService.emitirFactura(command));

        assertTrue(exception.getMessage().contains("EmpresaId es obligatorio"));
        verifyNoInteractions(facturaRepository);
        verifyNoInteractions(eventPublisher);
    }
}
