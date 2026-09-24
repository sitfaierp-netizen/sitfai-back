package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RecepcionMercanciaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RecepcionarMercanciaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarRecepcionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RecepcionarMercanciaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarRecepcionRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/**
 * Driving Adapter: REST Controller para la Recepción de Mercancías e Inbound Logistics.
 * <p>
 * Regla 1 (Aislamiento de Capas): Pertenece a infraestructura, orquesta puertos de entrada.
 * Regla 5 (API REST): Exposición formal con DTOs desacoplados del dominio y commands de aplicación.
 */
@RestController
@RequestMapping("/inventory")
public class RecepcionController {

    private final RegistrarRecepcionUseCase registrarRecepcionUseCase;
    private final RecepcionarMercanciaUseCase recepcionarMercanciaUseCase;

    public RecepcionController(
            RegistrarRecepcionUseCase registrarRecepcionUseCase,
            RecepcionarMercanciaUseCase recepcionarMercanciaUseCase) {
        this.registrarRecepcionUseCase = Objects.requireNonNull(registrarRecepcionUseCase, "RegistrarRecepcionUseCase es obligatorio");
        this.recepcionarMercanciaUseCase = Objects.requireNonNull(recepcionarMercanciaUseCase, "RecepcionarMercanciaUseCase es obligatorio");
    }

    /**
     * Endpoint legado para registrar una recepción general de mercancía.
     */
    @PostMapping("/recepciones")
    public ResponseEntity<Void> registrarRecepcion(@Valid @RequestBody RegistrarRecepcionRequest request) {
        UUID recepcionId = registrarRecepcionUseCase.registrar(request.toCommand());
        return ResponseEntity.created(URI.create("/inventory/recepciones/" + recepcionId)).build();
    }

    /**
     * Endpoint primario para Inbound Logistics (BOD-04, MT-01):
     * Recepción física de mercancía en una bodega específica amparada en una Orden de Compra.
     * POST /api/v1/inventory/bodegas/{id}/recepciones
     */
    @PostMapping("/bodegas/{id}/recepciones")
    public ResponseEntity<RecepcionMercanciaResponse> recepcionarMercancia(
            @PathVariable("id") UUID id,
            @Valid @RequestBody RecepcionarMercanciaWebRequest request) {

        RecepcionMercanciaResponse response = recepcionarMercanciaUseCase.recepcionar(request.toCommand(id));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
