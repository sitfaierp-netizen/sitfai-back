package com.SITFAI_CORE_ERP_TIENDA.billing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirNotaCreditoCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.NotaCreditoResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.FacturaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output.NotaCreditoRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.EstadoFacturaDian;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmitirNotaCreditoServiceTest {

    @Mock
    private NotaCreditoRepository notaCreditoRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private FacturaEventPublisher eventPublisher;

    @InjectMocks
    private EmitirNotaCreditoService service;

    private UUID empresaId;
    private UUID facturaId;
    private FacturaElectronica facturaSimulada;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        facturaId = UUID.randomUUID();
        
        // Simular factura original firmada
        ResolucionDian resolucion = new ResolucionDian("PRE", 1, 100, java.time.LocalDate.now().plusDays(10));
        facturaSimulada = FacturaElectronica.generarBorrador(
                new FacturaId(facturaId), new EmpresaId(empresaId), 
                new Nit("900111222"), new Nit("900333444"), resolucion
        );
        facturaSimulada.agregarLinea(new com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.LineaFactura("A", BigDecimal.ONE, Dinero.de(new BigDecimal("100"), "COP")));
        facturaSimulada.firmar(new Cufe("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"));
    }

    @Test
    void dadoComandoValidoYFacturaExistente_cuandoEmitir_entoncesGuardaBorrador() {
        EmitirNotaCreditoCommand command = new EmitirNotaCreditoCommand(
                empresaId, facturaId, "01", "Devolucion",
                List.of(new EmitirNotaCreditoCommand.LineaReversoDto(
                        "Devuelto", BigDecimal.ONE, new BigDecimal("100"), "COP", List.of()
                ))
        );

        when(facturaRepository.findByIdAndEmpresaId(any(FacturaId.class), any(UUID.class)))
                .thenReturn(Optional.of(facturaSimulada));

        NotaCreditoResponse response = service.emitirNotaCredito(command);

        assertNotNull(response);
        assertEquals(EstadoFacturaDian.BORRADOR.name(), response.estado());
        assertEquals(facturaId, response.facturaAfectadaId());
        assertEquals(new BigDecimal("100.00"), response.subtotal());

        verify(notaCreditoRepository).guardar(any());
    }

    @Test
    void dadoFacturaNoExistente_cuandoEmitir_entoncesLanzaDomainException() {
        EmitirNotaCreditoCommand command = new EmitirNotaCreditoCommand(
                empresaId, facturaId, "01", "Devolucion", List.of()
        );

        when(facturaRepository.findByIdAndEmpresaId(any(FacturaId.class), any(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> service.emitirNotaCredito(command));
        verify(notaCreditoRepository, never()).guardar(any());
    }
}
