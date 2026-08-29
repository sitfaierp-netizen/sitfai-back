package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearProductoCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CambiarEstadoProductoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CrearProductoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.ConsultarProductosUseCase;

/**
 * Driving Adapter: REST Controller para Producto.
 *
 * Principios aplicados:
 * - El request body no tiene el empresaId (se extrae en Service).
 * - Uso estricto de DTOs, no expone el Dominio (Regla 5).
 * - Seguridad por roles (EMPRESA_ADMIN, SUCURSAL_MANAGER).
 */
@RestController
@RequestMapping("/catalog/productos")
public class ProductoController {

    private final CrearProductoUseCase crearProductoUseCase;
    private final CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase;
    private final ConsultarProductosUseCase consultarProductosUseCase;

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                              CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase,
                              ConsultarProductosUseCase consultarProductosUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.cambiarEstadoProductoUseCase = cambiarEstadoProductoUseCase;
        this.consultarProductosUseCase = consultarProductosUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<ProductoResponse>> listarProductos() {
        List<ProductoResponse> response = consultarProductosUseCase.listarProductos();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ProductoResponse> crear(@RequestBody CrearProductoCommand command) {
        ProductoResponse response = crearProductoUseCase.crear(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productoId}/estado")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ProductoResponse> cambiarEstado(
            @PathVariable UUID productoId,
            @RequestBody CambiarEstadoRequest request) {
        ProductoResponse response = cambiarEstadoProductoUseCase.cambiarEstado(productoId, request.estado());
        return ResponseEntity.ok(response);
    }

    public record CambiarEstadoRequest(String estado) {}
}
