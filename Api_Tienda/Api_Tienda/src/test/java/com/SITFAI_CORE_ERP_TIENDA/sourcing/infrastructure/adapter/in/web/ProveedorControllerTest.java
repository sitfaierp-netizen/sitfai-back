package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.CrearProveedorCommand;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.ProveedorResponse;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.input.CrearProveedorUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProveedorControllerTest {

    private CrearProveedorUseCase crearProveedorUseCase;
    private ProveedorController proveedorController;

    @BeforeEach
    void setUp() {
        crearProveedorUseCase = mock(CrearProveedorUseCase.class);
        proveedorController = new ProveedorController(crearProveedorUseCase);
    }

    @Test
    void crearProveedor_exitoso() {
        CrearProveedorCommand command = new CrearProveedorCommand(
                "1234567890001", "Razon", "correo@test.com", "123", "Dir", 10
        );

        ProveedorResponse response = new ProveedorResponse(
                UUID.randomUUID(), UUID.randomUUID(), "1234567890001", "Razon",
                "correo@test.com", "123", "Dir", 10, "ACTIVO", Instant.now()
        );

        when(crearProveedorUseCase.crear(any())).thenReturn(response);

        ResponseEntity<ProveedorResponse> result = proveedorController.crear(command);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("1234567890001", result.getBody().ruc());
    }

    @Test
    void verificaRolesAutorizadosEnMetodoCrear() throws NoSuchMethodException {
        Method method = ProveedorController.class.getMethod("crear", CrearProveedorCommand.class);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize, "Debe tener @PreAuthorize");
        assertTrue(preAuthorize.value().contains("EMPRESA_ADMIN") || preAuthorize.value().contains("COMPRAS_MANAGER"), "Debe requerir rol EMPRESA_ADMIN o COMPRAS_MANAGER");
    }
}
