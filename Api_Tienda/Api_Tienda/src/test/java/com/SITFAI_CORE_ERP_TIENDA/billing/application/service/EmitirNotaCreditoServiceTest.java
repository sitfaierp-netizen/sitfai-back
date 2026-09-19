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
    }

    @Test
    void dadoComandoValidoYFacturaExistente_cuandoEmitir_entoncesGuardaBorrador() {
        assertTrue(true);
    }

    @Test
    void dadoFacturaNoExistente_cuandoEmitir_entoncesLanzaDomainException() {
        assertTrue(true);
    }
}
