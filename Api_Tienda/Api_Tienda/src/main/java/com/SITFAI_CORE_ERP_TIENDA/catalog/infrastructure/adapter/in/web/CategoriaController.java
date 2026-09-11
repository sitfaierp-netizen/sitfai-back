package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CategoriaResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearCategoriaCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.GestionarCategoriasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/catalog/categorias")
public class CategoriaController {

    private final GestionarCategoriasUseCase gestionarCategoriasUseCase;

    public CategoriaController(GestionarCategoriasUseCase gestionarCategoriasUseCase) {
        this.gestionarCategoriasUseCase = gestionarCategoriasUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<CategoriaResponse>> listarCategorias(@TenantId UUID empresaId) {
        List<CategoriaResponse> response = gestionarCategoriasUseCase.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<CategoriaResponse> crearCategoria(
            @TenantId UUID empresaId,
            @RequestBody CrearCategoriaCommand command) {
        CategoriaResponse response = gestionarCategoriasUseCase.crear(command, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
