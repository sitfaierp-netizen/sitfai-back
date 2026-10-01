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
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/empresas")
public class SucursalController {

    private final AgregarSucursalUseCase agregarSucursalUseCase;
    private final ActualizarSucursalUseCase actualizarSucursalUseCase;
    private final EliminarSucursalUseCase eliminarSucursalUseCase;
    private final CurrentTenantProvider currentTenantProvider;

    public SucursalController(
            AgregarSucursalUseCase agregarSucursalUseCase,
            ActualizarSucursalUseCase actualizarSucursalUseCase,
            EliminarSucursalUseCase eliminarSucursalUseCase,
            CurrentTenantProvider currentTenantProvider) {
        this.agregarSucursalUseCase = Objects.requireNonNull(agregarSucursalUseCase);
        this.actualizarSucursalUseCase = Objects.requireNonNull(actualizarSucursalUseCase);
        this.eliminarSucursalUseCase = Objects.requireNonNull(eliminarSucursalUseCase);
        this.currentTenantProvider = Objects.requireNonNull(currentTenantProvider);
    }

    /**
     * POST /empresas/{empresaId}/sucursales
     * Endpoint para crear una sucursal y asociarla a una empresa existente.
     */
    @PostMapping("/{empresaId}/sucursales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<SucursalResponse> agregarSucursal(
            @PathVariable UUID empresaId,
            @RequestBody AgregarSucursalRequest request) {
        
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(empresaId);
        AgregarSucursalCommand command = new AgregarSucursalCommand(tenantAutorizado, request.codigo(), request.nombre());
        SucursalResponse response = agregarSucursalUseCase.ejecutar(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /empresas/{empresaId}/sucursales/{sucursalId}
     */
    @PutMapping("/{empresaId}/sucursales/{sucursalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<SucursalResponse> actualizarSucursal(
            @PathVariable UUID empresaId,
            @PathVariable UUID sucursalId,
            @RequestBody ActualizarSucursalRequest request) {
        
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(empresaId);
        ActualizarSucursalCommand command = new ActualizarSucursalCommand(tenantAutorizado, sucursalId, request.codigo(), request.nombre());
        SucursalResponse response = actualizarSucursalUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /empresas/{empresaId}/sucursales/{sucursalId}
     */
    @DeleteMapping("/{empresaId}/sucursales/{sucursalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> eliminarSucursal(
            @PathVariable UUID empresaId,
            @PathVariable UUID sucursalId) {
        
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(empresaId);
        eliminarSucursalUseCase.ejecutar(tenantAutorizado, sucursalId);
        return ResponseEntity.noContent().build();
    }
}
