package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CerrarTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.GestionarTurnoCajaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.AbrirTurnoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.CerrarTurnoWebRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Driving Adapter: Controlador REST para la apertura y cierre de turnos de caja (POS).
 * <p>
 * Endpoints expuestos:
 * - POST /api/v1/pos/turnos/abrir
 * - POST /api/v1/pos/turnos/{id}/cerrar
 */
@RestController
@RequestMapping("/pos/turnos")
public class TurnoCajaController {

    private final GestionarTurnoCajaUseCase gestionarTurnoCajaUseCase;
    private final com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.RegistrarTransaccionCajaUseCase registrarTransaccionCajaUseCase;

    public TurnoCajaController(
            GestionarTurnoCajaUseCase gestionarTurnoCajaUseCase,
            com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.RegistrarTransaccionCajaUseCase registrarTransaccionCajaUseCase) {
        this.gestionarTurnoCajaUseCase = Objects.requireNonNull(gestionarTurnoCajaUseCase, "GestionarTurnoCajaUseCase es obligatorio");
        this.registrarTransaccionCajaUseCase = Objects.requireNonNull(registrarTransaccionCajaUseCase, "RegistrarTransaccionCajaUseCase es obligatorio");
    }

    /**
     * Endpoint para la apertura de turno de caja (Regla CAJ-02).
     */
    @PostMapping("/abrir")
    public ResponseEntity<TurnoCajaResponse> abrirTurno(@RequestBody AbrirTurnoWebRequest request) {
        UUID cajeroId = request.getCajeroId();
        var command = new AbrirTurnoCommand(
                request.cajaId(),
                cajeroId,
                request.montoApertura()
        );

        TurnoCajaResponse response = gestionarTurnoCajaUseCase.abrirTurno(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para el cierre de turno y arqueo financiero inmutable (Reglas CAJ-05 a CAJ-07 y DOC-01).
     */
    @PostMapping("/{id}/cerrar")
    public ResponseEntity<TurnoCajaResponse> cerrarTurno(
            @PathVariable UUID id,
            @RequestBody CerrarTurnoWebRequest request) {

        var command = new CerrarTurnoCommand(id, request.getMontoFisico());
        TurnoCajaResponse response = gestionarTurnoCajaUseCase.cerrarTurno(command);

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar transacciones de caja (Regla CAJ-03, CAJ-04 y BOD-04).
     */
    @PostMapping("/{id}/transacciones")
    public ResponseEntity<TurnoCajaResponse> registrarTransaccion(
            @PathVariable UUID id,
            @RequestBody com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.RegistrarTransaccionWebRequest request) {

        java.util.List<com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCajaCommand.LineaTransaccion> lineasCommand = null;
        if (request.lineas() != null) {
            lineasCommand = request.lineas().stream()
                    .map(l -> new com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCajaCommand.LineaTransaccion(l.productoId(), l.cantidad(), l.precioUnitario()))
                    .collect(java.util.stream.Collectors.toList());
        }

        var command = new com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCajaCommand(
                id,
                request.tipoTransaccion(),
                request.monto(),
                request.referencia(),
                lineasCommand
        );

        TurnoCajaResponse response = registrarTransaccionCajaUseCase.registrarTransaccion(command);

        return ResponseEntity.ok(response);
    }
}
