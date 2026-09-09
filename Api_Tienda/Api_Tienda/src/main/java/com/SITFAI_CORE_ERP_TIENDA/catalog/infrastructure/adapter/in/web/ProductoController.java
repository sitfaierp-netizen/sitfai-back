package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearProductoCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CambiarEstadoProductoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.CrearProductoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.VincularCodigoBarrasUseCase;
import jakarta.validation.Valid;
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
    private final VincularCodigoBarrasUseCase vincularCodigoBarrasUseCase;
    private final com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.EliminarProductoUseCase eliminarProductoUseCase;
    private final com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.ActualizarProductoUseCase actualizarProductoUseCase;

    public ProductoController(CrearProductoUseCase crearProductoUseCase,
                              CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase,
                              ConsultarProductosUseCase consultarProductosUseCase,
                              VincularCodigoBarrasUseCase vincularCodigoBarrasUseCase,
                              com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.EliminarProductoUseCase eliminarProductoUseCase,
                              com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.ActualizarProductoUseCase actualizarProductoUseCase) {
        this.crearProductoUseCase = crearProductoUseCase;
        this.cambiarEstadoProductoUseCase = cambiarEstadoProductoUseCase;
        this.consultarProductosUseCase = consultarProductosUseCase;
        this.vincularCodigoBarrasUseCase = vincularCodigoBarrasUseCase;
        this.eliminarProductoUseCase = eliminarProductoUseCase;
        this.actualizarProductoUseCase = actualizarProductoUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<ProductoResponse>> listarProductos(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<ProductoResponse> response = consultarProductosUseCase.listarProductos(search, org.springframework.data.domain.PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<ProductoResponse> crear(@RequestBody CrearProductoCommand command) {
        ProductoResponse response = crearProductoUseCase.crear(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{productoId}/estado")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<ProductoResponse> cambiarEstado(
            @PathVariable UUID productoId,
            @RequestBody CambiarEstadoRequest request) {
        ProductoResponse response = cambiarEstadoProductoUseCase.cambiarEstado(productoId, request.estado());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productoId}/barcode")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<ProductoResponse> actualizarCodigoBarras(
            @PathVariable UUID productoId,
            @Valid @RequestBody ActualizarCodigoBarrasRequest request) {
        ProductoResponse response = vincularCodigoBarrasUseCase.vincularCodigoBarras(productoId, request.codigoBarras());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productoId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID productoId) {
        eliminarProductoUseCase.eliminar(productoId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> actualizarProducto(
            @PathVariable UUID id, 
            @RequestBody @Valid com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input.ActualizarProductoRequest request) {
        actualizarProductoUseCase.actualizarProducto(id, request);
        return ResponseEntity.ok().build();
    }

    public record CambiarEstadoRequest(String estado) {}
    public record ActualizarCodigoBarrasRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 50) String codigoBarras) {}
}
