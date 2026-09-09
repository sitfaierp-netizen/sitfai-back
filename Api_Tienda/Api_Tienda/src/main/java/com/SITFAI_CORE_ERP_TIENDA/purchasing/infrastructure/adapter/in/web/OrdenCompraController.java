package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CambiarEstadoOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.GestionarLineasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.ListarOrdenesCompraUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST Controller: Endpoint Zero Trust para Compras.
 * Obligatoriedad de @TenantId en todos los métodos.
 */
@RestController
@RequestMapping("/api/v1/purchasing/ordenes")
public class OrdenCompraController {

    private final CrearOrdenUseCase crearOrdenUseCase;
    private final GestionarLineasUseCase gestionarLineasUseCase;
    private final CambiarEstadoOrdenUseCase cambiarEstadoOrdenUseCase;
    private final ListarOrdenesCompraUseCase listarOrdenesCompraUseCase;

    public OrdenCompraController(CrearOrdenUseCase crearOrdenUseCase,
                                 GestionarLineasUseCase gestionarLineasUseCase,
                                 CambiarEstadoOrdenUseCase cambiarEstadoOrdenUseCase,
                                 ListarOrdenesCompraUseCase listarOrdenesCompraUseCase) {
        this.crearOrdenUseCase = crearOrdenUseCase;
        this.gestionarLineasUseCase = gestionarLineasUseCase;
        this.cambiarEstadoOrdenUseCase = cambiarEstadoOrdenUseCase;
        this.listarOrdenesCompraUseCase = listarOrdenesCompraUseCase;
    }

    public record CrearOrdenRequest(UUID proveedorId, UUID bodegaDestinoId) {}
    public record AgregarLineaRequest(UUID productoId, BigDecimal cantidad, BigDecimal costoUnitario) {}
    public record CambiarEstadoRequest(String nuevoEstado) {}

    @GetMapping
    public ResponseEntity<Page<OrdenCompraResponse>> listarOrdenes(
            @RequestAttribute("TenantId") UUID tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrdenCompraResponse> response = listarOrdenesCompraUseCase.listarOrdenes(tenantId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrdenCompraResponse> crearBorrador(
            @RequestAttribute("TenantId") UUID tenantId,
            @RequestBody CrearOrdenRequest request) {

        CrearBorradorCommand command = new CrearBorradorCommand(tenantId, request.proveedorId(), request.bodegaDestinoId());
        OrdenCompraResponse response = crearOrdenUseCase.crearBorrador(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/lineas")
    public ResponseEntity<OrdenCompraResponse> agregarLinea(
            @RequestAttribute("TenantId") UUID tenantId,
            @PathVariable UUID id,
            @RequestBody AgregarLineaRequest request) {

        AgregarLineaCommand command = new AgregarLineaCommand(
                id, tenantId, request.productoId(), request.cantidad(), request.costoUnitario()
        );
        OrdenCompraResponse response = gestionarLineasUseCase.agregarLinea(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenCompraResponse> cambiarEstado(
            @RequestAttribute("TenantId") UUID tenantId,
            @PathVariable UUID id,
            @RequestBody CambiarEstadoRequest request) {

        CambiarEstadoCommand command = new CambiarEstadoCommand(
                id, tenantId, request.nuevoEstado()
        );
        OrdenCompraResponse response = cambiarEstadoOrdenUseCase.cambiarEstado(command);
        return ResponseEntity.ok(response);
    }
}
