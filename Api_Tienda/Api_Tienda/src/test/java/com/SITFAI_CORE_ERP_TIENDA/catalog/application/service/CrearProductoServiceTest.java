package com.SITFAI_CORE_ERP_TIENDA.catalog.application.service;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearProductoCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.CatalogEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.ProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CrearProductoServiceTest {

    private TenantProviderPort tenantProvider;
    private ProductoRepository productoRepository;
    private CatalogEventPublisherPort eventPublisher;
    private CrearProductoService crearProductoService;

    @BeforeEach
    void setUp() {
        tenantProvider = mock(TenantProviderPort.class);
        productoRepository = mock(ProductoRepository.class);
        eventPublisher = mock(CatalogEventPublisherPort.class);
        crearProductoService = new CrearProductoService(tenantProvider, productoRepository, eventPublisher);
    }

    @Test
    void debeCrearProductoYPublicarEvento() {
        UUID empresaId = UUID.randomUUID();
        when(tenantProvider.obtenerEmpresaIdActual()).thenReturn(empresaId);

        CrearProductoCommand command = new CrearProductoCommand(
                "SKU-001", "Prod", "Desc", UUID.randomUUID(),
                "UNIDAD", new BigDecimal("50.0"), new BigDecimal("100.0"),
                "IVA_19", "BARCODE"
        );

        ProductoResponse response = crearProductoService.crear(command);

        assertNotNull(response);
        assertEquals("SKU-001", response.sku());
        assertEquals(empresaId, response.empresaId());
        
        verify(productoRepository).guardar(any(Producto.class));
        verify(eventPublisher).publicar(any());
    }

    @Test
    void debePropagarExcepcionSiRepositoryFalla() {
        when(tenantProvider.obtenerEmpresaIdActual()).thenReturn(UUID.randomUUID());
        doThrow(new RuntimeException("Duplicado")).when(productoRepository).guardar(any(Producto.class));

        CrearProductoCommand command = new CrearProductoCommand(
                "SKU-001", "Prod", "Desc", UUID.randomUUID(),
                "UNIDAD", new BigDecimal("50.0"), new BigDecimal("100.0"),
                "IVA_19", "BARCODE"
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> crearProductoService.crear(command));
        assertEquals("Duplicado", ex.getMessage());
        
        verify(eventPublisher, never()).publicar(any());
    }
}
