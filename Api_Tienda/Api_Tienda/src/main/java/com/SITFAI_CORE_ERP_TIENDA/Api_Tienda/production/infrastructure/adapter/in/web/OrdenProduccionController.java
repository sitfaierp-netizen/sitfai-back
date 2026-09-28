package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.CompletarProduccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.IniciarProduccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.PlanificarOrdenCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.input.GestionarOrdenProduccionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.in.web.dto.PlanificarOrdenWebDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/production/orders")
public class OrdenProduccionController {

    private final GestionarOrdenProduccionUseCase gestionarOrdenProduccionUseCase;

    public OrdenProduccionController(GestionarOrdenProduccionUseCase gestionarOrdenProduccionUseCase) {
        this.gestionarOrdenProduccionUseCase = Objects.requireNonNull(gestionarOrdenProduccionUseCase);
    }

    @PostMapping
    public ResponseEntity<Void> planificarOrden(@RequestBody PlanificarOrdenWebDto dto) {
        PlanificarOrdenCommand command = new PlanificarOrdenCommand(dto.recetaId(), dto.bodegaId(), dto.cantidadProducir());
        UUID ordenId = gestionarOrdenProduccionUseCase.planificarOrden(command);
        return ResponseEntity.created(URI.create("/production/orders/" + ordenId)).build();
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<Void> iniciarProduccion(@PathVariable UUID id) {
        IniciarProduccionCommand command = new IniciarProduccionCommand(id);
        gestionarOrdenProduccionUseCase.iniciarProduccion(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<Void> completarProduccion(@PathVariable UUID id) {
        CompletarProduccionCommand command = new CompletarProduccionCommand(id);
        gestionarOrdenProduccionUseCase.completarProduccion(command);
        return ResponseEntity.ok().build();
    }
}
