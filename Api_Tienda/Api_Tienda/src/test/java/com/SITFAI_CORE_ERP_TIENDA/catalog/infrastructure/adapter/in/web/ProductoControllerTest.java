package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearProductoCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CambiarEstadoProductoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CrearProductoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductoControllerTest {

    private CrearProductoUseCase crearProductoUseCase;
    private CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase;
    private ProductoController productoController;

    @BeforeEach
    void setUp() {
        crearProductoUseCase = mock(CrearProductoUseCase.class);
        cambiarEstadoProductoUseCase = mock(CambiarEstadoProductoUseCase.class);
        productoController = new ProductoController(crearProductoUseCase, cambiarEstadoProductoUseCase);
    }

    @Test
    void crearProducto_exitoso() {
        CrearProductoCommand command = new CrearProductoCommand(
                "SKU", "Prod", "Desc", UUID.randomUUID(), "UNIDAD",
                new BigDecimal("10"), new BigDecimal("20"), "IVA_19", "BARCODE"
        );

        ProductoResponse response = new ProductoResponse(
                UUID.randomUUID(), UUID.randomUUID(), "SKU", "Prod", "Desc",
                command.categoriaId(), "UNIDAD", command.precioCompra(), command.precioVenta(),
                "IVA_19", "BARCODE", "ACTIVO", Instant.now(), Instant.now()
        );

        when(crearProductoUseCase.crear(any())).thenReturn(response);

        ResponseEntity<ProductoResponse> result = productoController.crear(command);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("SKU", result.getBody().sku());
    }

    @Test
    void verificaRolesAutorizadosEnMetodoCrear() throws NoSuchMethodException {
        Method method = ProductoController.class.getMethod("crear", CrearProductoCommand.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "Debe tener @PreAuthorize");
        assertTrue(preAuthorize.value().contains("EMPRESA_ADMIN"), "Debe requerir rol EMPRESA_ADMIN");
    }
}
