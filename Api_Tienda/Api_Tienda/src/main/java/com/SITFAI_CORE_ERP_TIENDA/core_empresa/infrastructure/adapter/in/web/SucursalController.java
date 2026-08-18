package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.AgregarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.AgregarSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.AgregarSucursalRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/empresas")
public class SucursalController {

    private final AgregarSucursalUseCase agregarSucursalUseCase;

    public SucursalController(AgregarSucursalUseCase agregarSucursalUseCase) {
        this.agregarSucursalUseCase = Objects.requireNonNull(agregarSucursalUseCase);
    }

    /**
     * POST /empresas/{empresaId}/sucursales
     * Endpoint para crear una sucursal y asociarla a una empresa existente.
     */
    @PostMapping("/{empresaId}/sucursales")
    public ResponseEntity<SucursalResponse> agregarSucursal(
            @PathVariable UUID empresaId,
            @RequestBody AgregarSucursalRequest request) {
        
        AgregarSucursalCommand command = new AgregarSucursalCommand(empresaId, request.codigo(), request.nombre());
        SucursalResponse response = agregarSucursalUseCase.ejecutar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
