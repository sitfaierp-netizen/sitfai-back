package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.ResolucionRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.EstadoFacturaDian;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.ResolucionDian;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmitirFacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;
    
    @Mock
    private ResolucionRepository resolucionRepository;

    @Mock
    private FacturaEventPublisher eventPublisher;

    @InjectMocks
    private EmitirFacturaService emitirFacturaService;

    private UUID empresaId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
    }

    @Test
    void dadoComandoValido_cuandoEmitirFactura_entoncesGuardaYRetornaBorrador() {
        // Arrange
        EmitirFacturaCommand.ImpuestoDto impuestoIva = new EmitirFacturaCommand.ImpuestoDto("IVA", new BigDecimal("19"));
        EmitirFacturaCommand.LineaFacturaDto linea = new EmitirFacturaCommand.LineaFacturaDto(
                "Consultoría", new BigDecimal("1"), new BigDecimal("100000"), "COP", List.of(impuestoIva));
        
        EmitirFacturaCommand command = new EmitirFacturaCommand(
                empresaId, "900111222", "800333444", List.of(linea)
        );

        ResolucionDian resolucion = new ResolucionDian("PRE", 1, 100, LocalDate.now().plusDays(10));
        when(resolucionRepository.obtenerActiva(new EmpresaId(empresaId))).thenReturn(Optional.of(resolucion));

        // Act
        FacturaResponse response = emitirFacturaService.emitirFactura(command);

        // Assert
        assertNotNull(response);
        assertEquals(EstadoFacturaDian.BORRADOR.name(), response.estado());
        assertEquals(new BigDecimal("100000.00"), response.subtotal());
        assertEquals(new BigDecimal("19000.00"), response.totalImpuestos());
        assertEquals(new BigDecimal("119000.00"), response.totalGeneral());
        
        verify(facturaRepository).guardar(any(FacturaElectronica.class));
        verify(eventPublisher, never()).publicar(any()); // No hay eventos en borrador
    }

    @Test
    void dadoComandoSinEmpresa_cuandoEmitirFactura_entoncesLanzaExcepcionMT01() {
        EmitirFacturaCommand command = new EmitirFacturaCommand(
                null, "900111222", "800333444", List.of()
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                emitirFacturaService.emitirFactura(command));
        
        assertTrue(exception.getMessage().contains("EmpresaId es obligatorio"));
        verifyNoInteractions(facturaRepository);
    }
}
