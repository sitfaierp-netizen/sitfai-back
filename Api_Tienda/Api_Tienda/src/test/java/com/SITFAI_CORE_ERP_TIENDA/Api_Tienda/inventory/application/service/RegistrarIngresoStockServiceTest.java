package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarIngresoStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistrarIngresoStockServiceTest {

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private TenantProviderPort tenantProvider;

    @Mock
    private BodegaEventPublisher eventPublisher;

    @InjectMocks
    private RegistrarIngresoStockService registrarIngresoStockService;

    private UUID bodegaIdUuid;
    private EmpresaId empresaId;
    private Bodega bodega;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bodegaIdUuid = UUID.randomUUID();
        empresaId = new EmpresaId(UUID.randomUUID());
        SucursalId sucursalId = new SucursalId(UUID.randomUUID());

        bodega = Bodega.crear(empresaId, sucursalId, "BOD-TEST", "Bodega Test", TipoBodega.VENTA);

        when(tenantProvider.getEmpresaIdAutenticada()).thenReturn(empresaId);
        when(bodegaRepository.buscarPorId(any(), any())).thenReturn(Optional.of(bodega));
    }

    @Test
    void testRegistrarIngreso_Exitoso() {
        UUID productoId = UUID.randomUUID();
        RegistrarIngresoStockCommand command = new RegistrarIngresoStockCommand(
                bodega.getId().valor(),
                productoId,
                BigDecimal.valueOf(10),
                "LOTE-1",
                Instant.now(),
                "COMPRA",
                "DOC-001"
        );

        registrarIngresoStockService.registrarIngreso(command);

        verify(bodegaRepository, times(1)).guardar(bodega);
        verify(eventPublisher, times(1)).publicarTodos(any());
    }
}
