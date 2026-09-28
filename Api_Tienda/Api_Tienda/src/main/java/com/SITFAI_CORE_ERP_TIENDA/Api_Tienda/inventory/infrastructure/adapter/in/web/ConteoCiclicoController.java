package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConteoCiclicoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.FinalizarConteoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.FinalizarConteoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarConteoFisicoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.ConteoCiclicoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarConteoFisicoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.mapper.ConteoWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Driving Adapter: REST Controller para la gestión y auditoría de Conteos Cíclicos (WMS).
 * <p>
 * Regla 5 (API REST): Rutas canónicas relativas y desacoplamiento mediante DTOs Web dedicados.
 * Rutas expuestas:
 * <ul>
 *   <li>PATCH /inventory/conteos/{id}/fisico</li>
 *   <li>POST /inventory/conteos/{id}/finalizar</li>
 * </ul>
 * Regla MT-02: No acepta empresaId en el payload ni URL (se extrae en la capa de aplicación).
 */
@RestController
@RequestMapping("/inventory/conteos")
public class ConteoCiclicoController {

    private final RegistrarConteoFisicoUseCase registrarConteoFisicoUseCase;
    private final FinalizarConteoUseCase finalizarConteoUseCase;

    public ConteoCiclicoController(
            RegistrarConteoFisicoUseCase registrarConteoFisicoUseCase,
            FinalizarConteoUseCase finalizarConteoUseCase) {
        this.registrarConteoFisicoUseCase = Objects.requireNonNull(registrarConteoFisicoUseCase, "RegistrarConteoFisicoUseCase es obligatorio.");
        this.finalizarConteoUseCase = Objects.requireNonNull(finalizarConteoUseCase, "FinalizarConteoUseCase es obligatorio.");
    }

    /**
     * Registra la cantidad física contada por un operario para un producto en estantería.
     */
    @PatchMapping("/{id}/fisico")
    public ResponseEntity<ConteoCiclicoWebResponse> registrarConteoFisico(
            @PathVariable("id") UUID conteoId,
            @Valid @RequestBody RegistrarConteoFisicoWebRequest request) {

        ConteoCiclicoResponse response = registrarConteoFisicoUseCase.registrar(
                ConteoWebMapper.toCommand(conteoId, request)
        );
        return ResponseEntity.ok(ConteoWebMapper.toWebResponse(response));
    }

    /**
     * Finaliza la auditoría del ciclo de conteo, evalúa descuadres y dispara eventos reactivos.
     */
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ConteoCiclicoWebResponse> finalizarConteo(
            @PathVariable("id") UUID conteoId) {

        ConteoCiclicoResponse response = finalizarConteoUseCase.finalizar(
                new FinalizarConteoCommand(conteoId)
        );
        return ResponseEntity.ok(ConteoWebMapper.toWebResponse(response));
    }
}
