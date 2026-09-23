package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ConfirmarDespachoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.ConfirmarDespachoWebRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Driving Adapter: REST Controller para la Logística de Salida (Outbound Logistics).
 * <p>
 * Regla 1 (Aislamiento): Pertenece a infraestructura, orquesta el caso de uso ConfirmarDespachoUseCase.
 * Regla 5 (API REST): Exposición formal bajo {@code POST /api/v1/inventory/despachos} desacoplando DTOs.
 */
@RestController
@RequestMapping({"/api/v1/inventory/despachos", "/inventory/despachos"})
public class DespachoController {

    private final ConfirmarDespachoUseCase confirmarDespachoUseCase;

    public DespachoController(ConfirmarDespachoUseCase confirmarDespachoUseCase) {
        this.confirmarDespachoUseCase = Objects.requireNonNull(confirmarDespachoUseCase, "ConfirmarDespachoUseCase es obligatorio");
    }

    /**
     * Confirma la salida física de mercancía amparada en un Pedido.
     * POST /api/v1/inventory/despachos
     */
    @PostMapping
    public ResponseEntity<DespachoResponse> confirmarDespacho(@Valid @RequestBody ConfirmarDespachoWebRequest request) {
        DespachoResponse response = confirmarDespachoUseCase.confirmarDespacho(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
