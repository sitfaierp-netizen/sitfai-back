package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.service;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.CrearProveedorCommand;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.ProveedorResponse;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.ProveedorRepository;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.SourcingEventPublisherPort;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.model.Proveedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CrearProveedorServiceTest {

    private TenantProviderPort tenantProvider;
    private ProveedorRepository proveedorRepository;
    private SourcingEventPublisherPort eventPublisher;
    private CrearProveedorService crearProveedorService;

    @BeforeEach
    void setUp() {
        tenantProvider = mock(TenantProviderPort.class);
        proveedorRepository = mock(ProveedorRepository.class);
        eventPublisher = mock(SourcingEventPublisherPort.class);
        crearProveedorService = new CrearProveedorService(tenantProvider, proveedorRepository, eventPublisher);
    }

    @Test
    void debeCrearProveedorYPublicarEvento() {
        UUID empresaId = UUID.randomUUID();
        when(tenantProvider.obtenerEmpresaIdActual()).thenReturn(empresaId);

        CrearProveedorCommand command = new CrearProveedorCommand(
                "1234567890001", "Razon", "correo@test.com", "123", "Dir", 10
        );

        ProveedorResponse response = crearProveedorService.crear(command);

        assertNotNull(response);
        assertEquals("1234567890001", response.ruc());
        assertEquals(empresaId, response.empresaId());
        assertEquals(10, response.plazoEntregaDias());
        
        verify(proveedorRepository).guardar(any(Proveedor.class));
        verify(eventPublisher).publicar(any());
    }

    @Test
    void debePropagarExcepcionSiRepositoryFalla() {
        when(tenantProvider.obtenerEmpresaIdActual()).thenReturn(UUID.randomUUID());
        doThrow(new RuntimeException("Duplicado")).when(proveedorRepository).guardar(any(Proveedor.class));

        CrearProveedorCommand command = new CrearProveedorCommand(
                "1234567890001", "Razon", "correo@test.com", "123", "Dir", 10
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> crearProveedorService.crear(command));
        assertEquals("Duplicado", ex.getMessage());
        
        verify(eventPublisher, never()).publicar(any());
    }
}
