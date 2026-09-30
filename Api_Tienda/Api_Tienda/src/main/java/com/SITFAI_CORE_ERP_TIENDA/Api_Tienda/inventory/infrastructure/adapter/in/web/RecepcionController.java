package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RecepcionMercanciaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RecepcionarMercanciaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RecepcionarMercanciaWebRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final RecepcionarMercanciaUseCase recepcionarMercanciaUseCase;

    public RecepcionController(RecepcionarMercanciaUseCase recepcionarMercanciaUseCase) {
        this.recepcionarMercanciaUseCase = Objects.requireNonNull(recepcionarMercanciaUseCase, "RecepcionarMercanciaUseCase es obligatorio");
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
