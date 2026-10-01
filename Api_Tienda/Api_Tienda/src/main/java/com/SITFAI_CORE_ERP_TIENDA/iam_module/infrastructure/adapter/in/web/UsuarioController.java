package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.CambiarRolUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.DesactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.ReactivarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.RegistrarUsuarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto.UsuarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.CambiarRolUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ConsultarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.DesactivarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ReactivarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.ReconciliarIdentidadUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.input.RegistrarUsuarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.CambiarRolUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.DesactivarUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.ReactivarUsuarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Driving Adapter: Controlador REST para la gestión de Identidad y Accesos (IAM).
 * Expone endpoints en /api/v1/iam/usuarios cumpliendo con MT-01 y RFC 7807.
 */
@RestController
@RequestMapping("/iam/usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final DesactivarUsuarioUseCase desactivarUsuarioUseCase;
    private final ReactivarUsuarioUseCase reactivarUsuarioUseCase;
    private final CambiarRolUsuarioUseCase cambiarRolUsuarioUseCase;
    private final ConsultarUsuarioUseCase consultarUsuarioUseCase;
    private final ReconciliarIdentidadUseCase reconciliarIdentidadUseCase;
    private final CurrentTenantProvider currentTenantProvider;

    public UsuarioController(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            DesactivarUsuarioUseCase desactivarUsuarioUseCase,
            ReactivarUsuarioUseCase reactivarUsuarioUseCase,
            CambiarRolUsuarioUseCase cambiarRolUsuarioUseCase,
            ConsultarUsuarioUseCase consultarUsuarioUseCase,
            ReconciliarIdentidadUseCase reconciliarIdentidadUseCase,
            CurrentTenantProvider currentTenantProvider
    ) {
        this.registrarUsuarioUseCase = Objects.requireNonNull(registrarUsuarioUseCase, "registrarUsuarioUseCase no puede ser null.");
        this.desactivarUsuarioUseCase = Objects.requireNonNull(desactivarUsuarioUseCase, "desactivarUsuarioUseCase no puede ser null.");
        this.reactivarUsuarioUseCase = Objects.requireNonNull(reactivarUsuarioUseCase, "reactivarUsuarioUseCase no puede ser null.");
        this.cambiarRolUsuarioUseCase = Objects.requireNonNull(cambiarRolUsuarioUseCase, "cambiarRolUsuarioUseCase no puede ser null.");
        this.consultarUsuarioUseCase = Objects.requireNonNull(consultarUsuarioUseCase, "consultarUsuarioUseCase no puede ser null.");
        this.reconciliarIdentidadUseCase = Objects.requireNonNull(reconciliarIdentidadUseCase, "reconciliarIdentidadUseCase no puede ser null.");
        this.currentTenantProvider = Objects.requireNonNull(currentTenantProvider, "currentTenantProvider no puede ser null.");
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody RegistrarUsuarioRequest request) {
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(request.empresaId());
        RegistrarUsuarioCommand command = UsuarioWebMapper.toCommand(tenantAutorizado, request);
        UsuarioResponse response = registrarUsuarioUseCase.ejecutar(command);
        URI location = URI.create("/iam/usuarios/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<UsuarioResponse> desactivar(
            @PathVariable UUID id,
            @RequestBody DesactivarUsuarioRequest request
    ) {
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(request.empresaId());
        DesactivarUsuarioCommand command = UsuarioWebMapper.toCommand(tenantAutorizado, id, request);
        UsuarioResponse response = desactivarUsuarioUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<UsuarioResponse> reactivar(
            @PathVariable UUID id,
            @RequestBody ReactivarUsuarioRequest request
    ) {
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(request.empresaId());
        ReactivarUsuarioCommand command = UsuarioWebMapper.toCommand(tenantAutorizado, id, request);
        UsuarioResponse response = reactivarUsuarioUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @PathVariable UUID id,
            @RequestBody CambiarRolUsuarioRequest request
    ) {
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(request.empresaId());
        CambiarRolUsuarioCommand command = UsuarioWebMapper.toCommand(tenantAutorizado, id, request);
        UsuarioResponse response = cambiarRolUsuarioUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reconciliar-identidad")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<UsuarioResponse> reconciliarIdentidad(
            @PathVariable UUID id,
            @RequestParam UUID empresaId
    ) {
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(empresaId);
        return ResponseEntity.ok(reconciliarIdentidadUseCase.ejecutar(tenantAutorizado, id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<UsuarioResponse> obtenerPorId(
            @PathVariable UUID id,
            @RequestParam UUID empresaId
    ) {
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(empresaId);
        UsuarioResponse response = consultarUsuarioUseCase.obtenerPorId(tenantAutorizado, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarPorEmpresa(
            @RequestParam UUID empresaId
    ) {
        UUID tenantAutorizado = currentTenantProvider.authorizeTenant(empresaId);
        List<UsuarioResponse> response = consultarUsuarioUseCase.listarPorEmpresa(tenantAutorizado);
        return ResponseEntity.ok(response);
    }
}
