package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DescontarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarIngresoStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.DescontarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarIngresoStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.DescontarStockRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarIngresoStockRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adaptador de Entrada REST (Driving Adapter) para Inventory.
 * <p>
 * Reglas validadas:
 * - REGLA-1: Controlador REST como adaptador.
 * - REGLA-5: Recibe DTOs Web y los mapea a Commands de Aplicación.
 * - REGLA-7: Seguridad implementada con @PreAuthorize y roles Keycloak.
 * - BOD-07: Solo BODEGA_OPERATOR y EMPRESA_ADMIN pueden operar inventarios.
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final RegistrarIngresoStockUseCase registrarIngresoStockUseCase;
    private final DescontarStockUseCase descontarStockUseCase;

    public InventoryController(
            RegistrarIngresoStockUseCase registrarIngresoStockUseCase,
            DescontarStockUseCase descontarStockUseCase) {
        this.registrarIngresoStockUseCase = registrarIngresoStockUseCase;
        this.descontarStockUseCase = descontarStockUseCase;
    }

    @PostMapping("/bodegas/{bodegaId}/ingresos")
    @PreAuthorize("hasRole('BODEGA_OPERATOR') or hasRole('EMPRESA_ADMIN')")
    public ResponseEntity<Void> registrarIngreso(
            @PathVariable UUID bodegaId,
            @Valid @RequestBody RegistrarIngresoStockRequest request) {

        RegistrarIngresoStockCommand command = new RegistrarIngresoStockCommand(
                bodegaId,
                request.productoId(),
                request.cantidad(),
                request.loteId(),
                request.fechaCaducidad(),
                request.docFuenteTipo(),
                request.docFuenteNumero()
        );

        registrarIngresoStockUseCase.registrarIngreso(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bodegas/{bodegaId}/egresos")
    @PreAuthorize("hasRole('BODEGA_OPERATOR') or hasRole('EMPRESA_ADMIN')")
    public ResponseEntity<Void> descontarStock(
            @PathVariable UUID bodegaId,
            @Valid @RequestBody DescontarStockRequest request) {

        DescontarStockCommand command = new DescontarStockCommand(
                bodegaId,
                request.productoId(),
                request.cantidad(),
                request.docFuenteTipo(),
                request.docFuenteNumero()
        );

        descontarStockUseCase.descontarStock(command);
        return ResponseEntity.ok().build();
    }
}
