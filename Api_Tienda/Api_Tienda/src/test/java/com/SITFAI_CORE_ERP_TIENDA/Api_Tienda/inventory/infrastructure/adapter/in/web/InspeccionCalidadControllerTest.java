package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.AprobarCuarentenaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.AprobarCuarentenaUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InspeccionCalidadControllerTest {

    @Mock
    private AprobarCuarentenaUseCase aproximarCuarentenaUseCase;

    @InjectMocks
    private InspeccionCalidadController controller;

    @Test
    void dadoRequestValido_cuandoAprobar_entoncesRetorna200() {
        UUID tenantId = UUID.randomUUID();
        InspeccionCalidadController.InspeccionRequest request = new InspeccionCalidadController.InspeccionRequest(
                UUID.randomUUID().toString(), UUID.randomUUID().toString(), new BigDecimal("10"), true
        );

        ResponseEntity<Void> response = controller.aprobarCuarentena(tenantId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(aproximarCuarentenaUseCase).aprobarCuarentena(any(AprobarCuarentenaCommand.class));
    }
}
