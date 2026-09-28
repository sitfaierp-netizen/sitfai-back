package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.ConfirmarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.EmpacarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.IniciarPickingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.GestionarDespachoUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/fulfillment/despachos")
public class OrdenDespachoController {

    private final GestionarDespachoUseCase gestionarDespachoUseCase;

    public OrdenDespachoController(GestionarDespachoUseCase gestionarDespachoUseCase) {
        this.gestionarDespachoUseCase = gestionarDespachoUseCase;
    }

    @PostMapping("/{id}/picking")
    public ResponseEntity<OrdenDespachoResponse> iniciarPicking(@PathVariable UUID id) {
        var response = gestionarDespachoUseCase.iniciarPicking(new IniciarPickingCommand(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/empacar")
    public ResponseEntity<OrdenDespachoResponse> empacar(@PathVariable UUID id) {
        var response = gestionarDespachoUseCase.empacar(new EmpacarDespachoCommand(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<OrdenDespachoResponse> confirmar(@PathVariable UUID id) {
        var response = gestionarDespachoUseCase.confirmar(new ConfirmarDespachoCommand(id));
        return ResponseEntity.ok(response);
    }
}
