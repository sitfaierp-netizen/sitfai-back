package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.EmitirOrdenCompraCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CambiarEstadoOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearOrdenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.EmitirOrdenCompraUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.GestionarLineasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto.EmitirOrdenCompraWebRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Driving Adapter: REST Controller para Compras / Abastecimiento (Puerto 8087).
 * <p>
 * Regla 5: Exposición bajo versión formal {@code /api/v1/purchasing/ordenes}.
 * Regla MT-02: Ningún parámetro de empresa en payload o query; se resuelve por tokens seguros.
 */
@RestController
@RequestMapping("/purchasing/ordenes")
public class OrdenCompraController {

    private final EmitirOrdenCompraUseCase emitirOrdenCompraUseCase;
    private final CrearOrdenUseCase crearOrdenUseCase;
    private final GestionarLineasUseCase gestionarLineasUseCase;
    private final CambiarEstadoOrdenUseCase cambiarEstadoOrdenUseCase;

    public OrdenCompraController(
            EmitirOrdenCompraUseCase emitirOrdenCompraUseCase,
            CrearOrdenUseCase crearOrdenUseCase,
            GestionarLineasUseCase gestionarLineasUseCase,
            CambiarEstadoOrdenUseCase cambiarEstadoOrdenUseCase) {
        this.emitirOrdenCompraUseCase = emitirOrdenCompraUseCase;
        this.crearOrdenUseCase = crearOrdenUseCase;
        this.gestionarLineasUseCase = gestionarLineasUseCase;
        this.cambiarEstadoOrdenUseCase = cambiarEstadoOrdenUseCase;
    }

    public record CrearOrdenRequest(UUID proveedorId) {}
    public record AgregarLineaRequest(UUID productoId, BigDecimal cantidad, BigDecimal costoUnitario) {}
    public record CambiarEstadoRequest(String nuevoEstado) {}

    /**
     * Endpoint Primario (BOD-04, MT-01): Emite formalmente una Orden de Compra al proveedor.
     */
    @PostMapping
    public ResponseEntity<OrdenCompraResponse> emitirOrdenCompra(
            @Valid @RequestBody EmitirOrdenCompraWebRequest request) {

        List<EmitirOrdenCompraCommand.LineaOrdenCompraCommand> lineasCmd = request.lineas().stream()
                .map(l -> new EmitirOrdenCompraCommand.LineaOrdenCompraCommand(
                        l.productoId(),
                        l.cantidadSolicitada(),
                        l.costoUnitarioEsperado()
                ))
                .toList();

        EmitirOrdenCompraCommand command = new EmitirOrdenCompraCommand(request.proveedorId(), lineasCmd);
        OrdenCompraResponse response = emitirOrdenCompraUseCase.emitirOrdenCompra(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint legado para crear borrador preliminar.
     */
    @PostMapping("/borrador")
    public ResponseEntity<OrdenCompraResponse> crearBorrador(
            @RequestAttribute("TenantId") UUID tenantId,
            @RequestBody CrearOrdenRequest request) {

        CrearBorradorCommand command = new CrearBorradorCommand(tenantId, request.proveedorId());
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
