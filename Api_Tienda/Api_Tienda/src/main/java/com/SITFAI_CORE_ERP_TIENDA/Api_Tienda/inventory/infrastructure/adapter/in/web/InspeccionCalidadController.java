package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.AprobarCuarentenaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.AprobarCuarentenaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST Controller: Endpoint protegido para Inspectores de Calidad.
 */
@RestController
@RequestMapping("/inventory/cuarentena")
public class InspeccionCalidadController {

    private final AprobarCuarentenaUseCase aproximarCuarentenaUseCase;

    public InspeccionCalidadController(AprobarCuarentenaUseCase aproximarCuarentenaUseCase) {
        this.aproximarCuarentenaUseCase = aproximarCuarentenaUseCase;
    }

    public record InspeccionRequest(String sucursalId, String productoId, BigDecimal cantidad, boolean aprobado) {}

    @PostMapping("/aprobar")
    @PreAuthorize("hasRole('INSPECTOR') or hasRole('BODEGA_ADMIN')")
    public ResponseEntity<Void> aprobarCuarentena(
            @RequestAttribute("TenantId") UUID tenantId,
            @RequestBody InspeccionRequest request) {

        AprobarCuarentenaCommand command = new AprobarCuarentenaCommand(
                tenantId.toString(),
                request.sucursalId(),
                request.productoId(),
                request.cantidad(),
                request.aprobado()
        );

        aproximarCuarentenaUseCase.aprobarCuarentena(command);

        return ResponseEntity.ok().build();
    }
}
