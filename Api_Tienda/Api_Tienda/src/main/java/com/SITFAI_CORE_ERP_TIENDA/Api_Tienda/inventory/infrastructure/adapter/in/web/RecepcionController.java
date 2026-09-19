package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarRecepcionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarRecepcionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory/recepciones")
public class RecepcionController {

    private final RegistrarRecepcionUseCase registrarRecepcionUseCase;

    public RecepcionController(RegistrarRecepcionUseCase registrarRecepcionUseCase) {
        this.registrarRecepcionUseCase = registrarRecepcionUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> registrarRecepcion(@Valid @RequestBody RegistrarRecepcionRequest request) {
        UUID recepcionId = registrarRecepcionUseCase.registrar(request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/inventory/recepciones/" + recepcionId)).build();
    }
}
