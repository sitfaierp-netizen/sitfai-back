package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.AbrirTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.CerrarTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.ConsultarTurnoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.RegistrarTransaccionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.AbrirTurnoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.CerrarTurnoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.RegistrarTransaccionWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/pos/turnos")
public class TurnoCajaController {

    private final AbrirTurnoUseCase abrirTurnoUseCase;
    private final RegistrarTransaccionUseCase registrarTransaccionUseCase;
    private final CerrarTurnoUseCase cerrarTurnoUseCase;
    private final ConsultarTurnoUseCase consultarTurnoUseCase;

    public TurnoCajaController(
            AbrirTurnoUseCase abrirTurnoUseCase,
            RegistrarTransaccionUseCase registrarTransaccionUseCase,
            CerrarTurnoUseCase cerrarTurnoUseCase,
            ConsultarTurnoUseCase consultarTurnoUseCase) {
        this.abrirTurnoUseCase = Objects.requireNonNull(abrirTurnoUseCase);
        this.registrarTransaccionUseCase = Objects.requireNonNull(registrarTransaccionUseCase);
        this.cerrarTurnoUseCase = Objects.requireNonNull(cerrarTurnoUseCase);
        this.consultarTurnoUseCase = Objects.requireNonNull(consultarTurnoUseCase);
    }

    @PostMapping
    public ResponseEntity<TurnoCajaResponse> abrirTurno(
            @TenantId UUID empresaId,
            @RequestBody AbrirTurnoWebRequest request) {

        var command = TurnoCajaWebMapper.toCommand(empresaId, request);
        var response = abrirTurnoUseCase.ejecutar(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/transacciones")
    public ResponseEntity<TurnoCajaResponse> registrarTransaccion(
            @TenantId UUID empresaId,
            @PathVariable UUID id,
            @RequestBody RegistrarTransaccionWebRequest request) {

        var command = TurnoCajaWebMapper.toCommand(empresaId, id, request);
        var response = registrarTransaccionUseCase.ejecutar(command);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<TurnoCajaResponse> cerrarTurno(
            @TenantId UUID empresaId,
            @PathVariable UUID id,
            @RequestBody CerrarTurnoWebRequest request) {

        var command = TurnoCajaWebMapper.toCommand(empresaId, id, request);
        var response = cerrarTurnoUseCase.ejecutar(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoCajaResponse> consultarTurno(
            @TenantId UUID empresaId,
            @PathVariable UUID id) {

        var response = consultarTurnoUseCase.porId(id, empresaId);
        return ResponseEntity.ok(response);
    }
}
