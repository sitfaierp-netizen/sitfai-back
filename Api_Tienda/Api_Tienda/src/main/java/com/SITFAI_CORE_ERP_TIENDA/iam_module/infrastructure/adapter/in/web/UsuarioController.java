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

    public UsuarioController(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            DesactivarUsuarioUseCase desactivarUsuarioUseCase,
            ReactivarUsuarioUseCase reactivarUsuarioUseCase,
            CambiarRolUsuarioUseCase cambiarRolUsuarioUseCase,
            ConsultarUsuarioUseCase consultarUsuarioUseCase
    ) {
        this.registrarUsuarioUseCase = Objects.requireNonNull(registrarUsuarioUseCase, "registrarUsuarioUseCase no puede ser null.");
        this.desactivarUsuarioUseCase = Objects.requireNonNull(desactivarUsuarioUseCase, "desactivarUsuarioUseCase no puede ser null.");
        this.reactivarUsuarioUseCase = Objects.requireNonNull(reactivarUsuarioUseCase, "reactivarUsuarioUseCase no puede ser null.");
        this.cambiarRolUsuarioUseCase = Objects.requireNonNull(cambiarRolUsuarioUseCase, "cambiarRolUsuarioUseCase no puede ser null.");
        this.consultarUsuarioUseCase = Objects.requireNonNull(consultarUsuarioUseCase, "consultarUsuarioUseCase no puede ser null.");
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody RegistrarUsuarioRequest request) {
        RegistrarUsuarioCommand command = UsuarioWebMapper.toCommand(request);
        UsuarioResponse response = registrarUsuarioUseCase.ejecutar(command);
        URI location = URI.create("/iam/usuarios/" + response.id() + "?empresaId=" + response.empresaId());
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponse> desactivar(
            @PathVariable UUID id,
            @RequestBody DesactivarUsuarioRequest request
    ) {
        DesactivarUsuarioCommand command = UsuarioWebMapper.toCommand(id, request);
        UsuarioResponse response = desactivarUsuarioUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<UsuarioResponse> reactivar(
            @PathVariable UUID id,
            @RequestBody ReactivarUsuarioRequest request
    ) {
        ReactivarUsuarioCommand command = UsuarioWebMapper.toCommand(id, request);
        UsuarioResponse response = reactivarUsuarioUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @PathVariable UUID id,
            @RequestBody CambiarRolUsuarioRequest request
    ) {
        CambiarRolUsuarioCommand command = UsuarioWebMapper.toCommand(id, request);
        UsuarioResponse response = cambiarRolUsuarioUseCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(
            @PathVariable UUID id,
            @RequestParam UUID empresaId
    ) {
        UsuarioResponse response = consultarUsuarioUseCase.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarPorEmpresa(
            @RequestParam UUID empresaId
    ) {
        List<UsuarioResponse> response = consultarUsuarioUseCase.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(response);
    }
}
