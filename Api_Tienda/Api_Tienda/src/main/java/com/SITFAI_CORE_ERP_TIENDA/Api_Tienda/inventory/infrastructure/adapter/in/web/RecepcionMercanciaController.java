package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CompletarRecepcionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarProductoRecibidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.GestionarRecepcionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarProductoWebDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/inventory/recepciones")
public class RecepcionMercanciaController {

    private final GestionarRecepcionUseCase useCase;

    public RecepcionMercanciaController(GestionarRecepcionUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase);
    }

    @PatchMapping("/{id}/productos")
    public ResponseEntity<Void> registrarProducto(@PathVariable UUID id, @RequestBody RegistrarProductoWebDto dto) {
        RegistrarProductoRecibidoCommand command = new RegistrarProductoRecibidoCommand(
                id,
                dto.getProductoId(),
                dto.getCantidad()
        );
        useCase.registrarProductoRecibido(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<Void> completarRecepcion(@PathVariable UUID id) {
        CompletarRecepcionCommand command = new CompletarRecepcionCommand(id);
        useCase.completarRecepcion(command);
        return ResponseEntity.ok().build();
    }
}
