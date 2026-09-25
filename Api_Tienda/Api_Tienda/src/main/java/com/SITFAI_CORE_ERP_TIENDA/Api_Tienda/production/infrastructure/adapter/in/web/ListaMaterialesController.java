package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.AgregarComponenteCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.AprobarRecetaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.CrearBorradorRecetaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.input.GestionarListaMaterialesUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.in.web.dto.AgregarComponenteWebDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.in.web.dto.CrearBorradorWebDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/production/bom")
public class ListaMaterialesController {

    private final GestionarListaMaterialesUseCase gestionarListaMaterialesUseCase;

    public ListaMaterialesController(GestionarListaMaterialesUseCase gestionarListaMaterialesUseCase) {
        this.gestionarListaMaterialesUseCase = Objects.requireNonNull(gestionarListaMaterialesUseCase);
    }

    @PostMapping
    public ResponseEntity<Void> crearBorrador(@RequestBody CrearBorradorWebDto dto) {
        CrearBorradorRecetaCommand command = new CrearBorradorRecetaCommand(dto.productoFinalId());
        UUID recetaId = gestionarListaMaterialesUseCase.crearBorradorReceta(command);
        return ResponseEntity.created(URI.create("/production/bom/" + recetaId)).build();
    }

    @PatchMapping("/{id}/componentes")
    public ResponseEntity<Void> agregarComponente(@PathVariable UUID id, @RequestBody AgregarComponenteWebDto dto) {
        AgregarComponenteCommand command = new AgregarComponenteCommand(id, dto.insumoId(), dto.cantidad());
        gestionarListaMaterialesUseCase.agregarComponente(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Void> aprobarReceta(@PathVariable UUID id) {
        AprobarRecetaCommand command = new AprobarRecetaCommand(id);
        gestionarListaMaterialesUseCase.aprobarReceta(command);
        return ResponseEntity.ok().build();
    }
}
