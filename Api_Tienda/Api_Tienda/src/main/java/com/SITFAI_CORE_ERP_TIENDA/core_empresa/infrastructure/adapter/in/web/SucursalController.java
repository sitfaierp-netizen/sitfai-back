package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.AgregarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.AgregarSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.ActualizarSucursalCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.ActualizarSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input.EliminarSucursalUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web.dto.ActualizarSucursalRequest;
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
    private final ActualizarSucursalUseCase actualizarSucursalUseCase;
    private final EliminarSucursalUseCase eliminarSucursalUseCase;

    public SucursalController(
            AgregarSucursalUseCase agregarSucursalUseCase,
            ActualizarSucursalUseCase actualizarSucursalUseCase,
            EliminarSucursalUseCase eliminarSucursalUseCase) {
        this.agregarSucursalUseCase = Objects.requireNonNull(agregarSucursalUseCase);
        this.actualizarSucursalUseCase = Objects.requireNonNull(actualizarSucursalUseCase);
        this.eliminarSucursalUseCase = Objects.requireNonNull(eliminarSucursalUseCase);
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

    /**
     * PUT /empresas/{empresaId}/sucursales/{sucursalId}
     */
    @PutMapping("/{empresaId}/sucursales/{sucursalId}")
    public ResponseEntity<SucursalResponse> actualizarSucursal(
            @PathVariable UUID empresaId,
            @PathVariable UUID sucursalId,
            @RequestBody ActualizarSucursalRequest request) {
        
        ActualizarSucursalCommand command = new ActualizarSucursalCommand(empresaId, sucursalId, request.codigo(), request.nombre());
        SucursalResponse response = actualizarSucursalUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /empresas/{empresaId}/sucursales/{sucursalId}
     */
    @DeleteMapping("/{empresaId}/sucursales/{sucursalId}")
    public ResponseEntity<Void> eliminarSucursal(
            @PathVariable UUID empresaId,
            @PathVariable UUID sucursalId) {
        
        eliminarSucursalUseCase.ejecutar(empresaId, sucursalId);
        return ResponseEntity.noContent().build();
    }
}
