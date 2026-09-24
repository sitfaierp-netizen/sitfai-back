package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AprobarSolicitudCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearSolicitudCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.SolicitudResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.AprobarSolicitudUseCase;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearSolicitudUseCase;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Driving Adapter: REST Controller para SolicitudAbastecimiento.
 *
 * Principios aplicados:
 * - Zero Trust: el empresaId proviene del JWT (@TenantId), NUNCA del body (MT-01, MT-06).
 * - Usa DTOs de Web (Request/Response) — el Dominio nunca cruza la frontera HTTP (Regla 5).
 * - Errores manejados por PurchasingExceptionHandler (RFC 7807).
 */
@RestController
@RequestMapping("/purchasing/solicitudes")
public class SolicitudAbastecimientoController {

    private final CrearSolicitudUseCase crearSolicitudUseCase;
    private final AprobarSolicitudUseCase aprobarSolicitudUseCase;

    public SolicitudAbastecimientoController(
            CrearSolicitudUseCase crearSolicitudUseCase,
            AprobarSolicitudUseCase aprobarSolicitudUseCase) {
        this.crearSolicitudUseCase   = crearSolicitudUseCase;
        this.aprobarSolicitudUseCase = aprobarSolicitudUseCase;
    }

    /**
     * POST /api/v1/purchasing/solicitudes
     * Crea y envía a aprobación una solicitud de abastecimiento.
     * Rol requerido: BODEGA_OPERATOR o superior.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BODEGA_OPERATOR','SUCURSAL_MANAGER','EMPRESA_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SolicitudResponse> crear(
            @TenantId UUID empresaId,
            @RequestBody CrearSolicitudWebRequest request) {

        CrearSolicitudCommand command = new CrearSolicitudCommand(
                empresaId,
                request.bodegaId(),
                request.lineas().stream()
                        .map(l -> new CrearSolicitudCommand.LineaSolicitudCommand(
                                l.productoId(), l.cantidadSolicitada()))
                        .toList()
        );

        SolicitudResponse response = crearSolicitudUseCase.crear(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/v1/purchasing/solicitudes/{id}/aprobar
     * Aprueba una solicitud en estado PENDIENTE_APROBACION.
     * Rol requerido: SUCURSAL_MANAGER o superior.
     */
    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('SUCURSAL_MANAGER','EMPRESA_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SolicitudResponse> aprobar(
            @TenantId UUID empresaId,
            @PathVariable UUID id) {

        AprobarSolicitudCommand command = new AprobarSolicitudCommand(id, empresaId);
        SolicitudResponse response = aprobarSolicitudUseCase.aprobar(command);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Web DTOs internos — evitan exponer el dominio al HTTP layer (Regla 5)
    // -------------------------------------------------------------------------

    public record CrearSolicitudWebRequest(
            UUID bodegaId,
            List<LineaWebRequest> lineas
    ) {
        public record LineaWebRequest(
                UUID productoId,
                BigDecimal cantidadSolicitada
        ) {}
    }
}
