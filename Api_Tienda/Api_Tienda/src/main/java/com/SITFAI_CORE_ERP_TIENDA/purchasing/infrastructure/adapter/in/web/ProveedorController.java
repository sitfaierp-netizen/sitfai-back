package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearProveedorRequest;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.ProveedorResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearProveedorUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController("purchasingProveedorController")
@RequestMapping("/purchasing/proveedores")
public class ProveedorController {

    private final CrearProveedorUseCase crearProveedorUseCase;

    public ProveedorController(CrearProveedorUseCase crearProveedorUseCase) {
        this.crearProveedorUseCase = crearProveedorUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listarProveedores(@RequestAttribute("TenantId") UUID tenantId) {
        List<ProveedorResponse> response = crearProveedorUseCase.listarProveedores(tenantId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> crearProveedor(
            @RequestAttribute("TenantId") UUID tenantId,
            @Valid @RequestBody CrearProveedorRequest request) {
        crearProveedorUseCase.crearProveedor(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
