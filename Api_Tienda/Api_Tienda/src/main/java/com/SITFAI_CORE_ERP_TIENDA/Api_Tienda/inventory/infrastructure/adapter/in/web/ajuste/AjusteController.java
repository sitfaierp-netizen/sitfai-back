package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.ajuste;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarAjusteCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarAjusteUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.AjusteInventarioId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/inventory/ajustes")
public class AjusteController {

    private final RegistrarAjusteUseCase registrarAjusteUseCase;

    public AjusteController(RegistrarAjusteUseCase registrarAjusteUseCase) {
        this.registrarAjusteUseCase = registrarAjusteUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST_STOCK')")
    public ResponseEntity<Map<String, String>> registrarAjuste(@RequestBody RegistrarAjusteCommand command) {
        AjusteInventarioId ajusteId = registrarAjusteUseCase.ejecutar(command);
        return ResponseEntity
                .created(URI.create("/inventory/ajustes/" + ajusteId.valor()))
                .body(Map.of("id", ajusteId.valor().toString(), "status", "SUCCESS"));
    }
}
