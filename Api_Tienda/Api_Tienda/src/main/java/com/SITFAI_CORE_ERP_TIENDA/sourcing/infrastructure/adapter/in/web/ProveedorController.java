package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.CrearProveedorCommand;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto.ProveedorResponse;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.input.CrearProveedorUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sourcing/proveedores")
public class ProveedorController {

    private final CrearProveedorUseCase crearProveedorUseCase;

    public ProveedorController(CrearProveedorUseCase crearProveedorUseCase) {
        this.crearProveedorUseCase = crearProveedorUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPRESA_ADMIN') or hasRole('COMPRAS_MANAGER')")
    public ResponseEntity<ProveedorResponse> crear(@RequestBody CrearProveedorCommand command) {
        ProveedorResponse response = crearProveedorUseCase.crear(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
