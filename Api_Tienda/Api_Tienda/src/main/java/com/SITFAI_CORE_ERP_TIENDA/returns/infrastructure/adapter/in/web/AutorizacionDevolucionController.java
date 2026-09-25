package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.AutorizacionDevolucionResponse;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.CrearAutorizacionDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.InspeccionarDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.LineaDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.port.input.GestionarDevolucionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web.dto.CrearRmaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web.dto.InspeccionarRmaWebRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/returns/rma")
public class AutorizacionDevolucionController {

    private final GestionarDevolucionUseCase gestionarDevolucionUseCase;

    public AutorizacionDevolucionController(GestionarDevolucionUseCase gestionarDevolucionUseCase) {
        this.gestionarDevolucionUseCase = gestionarDevolucionUseCase;
    }

    @PostMapping
    public ResponseEntity<AutorizacionDevolucionResponse> crearRma(@RequestBody CrearRmaWebRequest request) {
        var lineas = request.lineas().stream()
                .map(l -> new LineaDevolucionCommand(l.productoId(), l.cantidad(), l.motivo()))
                .collect(Collectors.toList());
                
        var command = new CrearAutorizacionDevolucionCommand(request.documentoFuenteId(), lineas);
        var response = gestionarDevolucionUseCase.crearAutorizacion(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/inspeccionar")
    public ResponseEntity<AutorizacionDevolucionResponse> inspeccionarRma(
            @PathVariable UUID id,
            @RequestBody InspeccionarRmaWebRequest request) {
            
        var command = new InspeccionarDevolucionCommand(id, request.productoId(), request.aprobado());
        var response = gestionarDevolucionUseCase.inspeccionar(command);
        return ResponseEntity.ok(response);
    }
}
