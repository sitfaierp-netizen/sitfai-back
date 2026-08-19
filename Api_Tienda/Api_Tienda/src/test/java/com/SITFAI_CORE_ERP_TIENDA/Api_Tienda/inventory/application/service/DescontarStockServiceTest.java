package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DescontarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.StockInsuficienteException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DescontarStockServiceTest {

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private TenantProviderPort tenantProvider;

    @Mock
    private BodegaEventPublisher eventPublisher;

    @InjectMocks
    private DescontarStockService descontarStockService;

    private UUID bodegaIdUuid;
    private EmpresaId empresaId;
    private Bodega bodega;
    private ProductoId productoId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bodegaIdUuid = UUID.randomUUID();
        empresaId = new EmpresaId(UUID.randomUUID());
        SucursalId sucursalId = new SucursalId(UUID.randomUUID());
        productoId = new ProductoId(UUID.randomUUID());

        bodega = Bodega.crear(empresaId, sucursalId, "BOD-TEST", "Bodega Test", TipoBodega.VENTA);

        when(tenantProvider.getEmpresaIdAutenticada()).thenReturn(empresaId);
        when(bodegaRepository.buscarPorId(any(), any())).thenReturn(Optional.of(bodega));
    }

    @Test
    void testDescontarStock_Exitoso() {
        // Pre-cargar 10 unidades
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(10)), LoteId.de("LOTE-A"), Instant.now(), new DocumentoFuenteId("COMPRA", "OC-001"));
        bodega.drainDomainEvents(); // limpiar eventos iniciales

        DescontarStockCommand command = new DescontarStockCommand(
                bodega.getId().valor(),
                productoId.valor(),
                BigDecimal.valueOf(5),
                "VENTA",
                "VEN-001"
        );

        descontarStockService.descontarStock(command);

        verify(bodegaRepository, times(1)).guardar(bodega);
        verify(eventPublisher, times(1)).publicarTodos(any());
    }

    @Test
    void testDescontarStock_StockInsuficiente() {
        // Bodega vacía
        DescontarStockCommand command = new DescontarStockCommand(
                bodega.getId().valor(),
                productoId.valor(),
                BigDecimal.valueOf(5),
                "VENTA",
                "VEN-001"
        );

        assertThrows(StockInsuficienteException.class, () -> {
            descontarStockService.descontarStock(command);
        });

        verify(bodegaRepository, never()).guardar(any());
    }
}
