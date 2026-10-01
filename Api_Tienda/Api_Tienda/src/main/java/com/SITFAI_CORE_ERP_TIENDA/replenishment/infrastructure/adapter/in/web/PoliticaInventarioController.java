package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.GestionarPoliticaInventarioCommand;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.PoliticaInventarioResult;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.exception.PoliticaInventarioNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.port.input.GestionarPoliticaInventarioUseCase;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.in.web.dto.PoliticaInventarioRequest;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.in.web.dto.PoliticaInventarioResponse;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Driving Adapter: REST Controller para Políticas de Inventario del módulo Replenishment.
 * <p>
 * Regla REGLA-5: Versionado /api/v1/, DTOs de Web desacoplados del dominio, nunca retorna objetos de Dominio.
 * Regla MT-01: El empresaId proviene del JWT (@TenantId), nunca del body del cliente.
 * Regla MT-02: {@code empresa_id} está PROHIBIDO en el body.
 */
@RestController
@RequestMapping("/api/v1/replenishment/politicas")
public class PoliticaInventarioController {

    private final GestionarPoliticaInventarioUseCase gestionarPoliticaUseCase;

    public PoliticaInventarioController(GestionarPoliticaInventarioUseCase gestionarPoliticaUseCase) {
        this.gestionarPoliticaUseCase = gestionarPoliticaUseCase;
    }

    /**
     * POST /api/v1/replenishment/politicas
     * Configura una nueva política de reposición automática para un producto en una bodega.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BODEGA_OPERATOR','SUCURSAL_MANAGER','EMPRESA_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<PoliticaInventarioResponse> crear(
            @TenantId UUID empresaId,
            @RequestBody PoliticaInventarioRequest request) {

        PoliticaInventarioResult politica = gestionarPoliticaUseCase.crear(toCommand(null, empresaId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(politica));
    }

    /**
     * PUT /api/v1/replenishment/politicas/{id}
     * Actualiza una política existente (cambia umbrales o estado activo/inactivo).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BODEGA_OPERATOR','SUCURSAL_MANAGER','EMPRESA_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<PoliticaInventarioResponse> actualizar(
            @TenantId UUID empresaId,
            @PathVariable UUID id,
            @RequestBody PoliticaInventarioRequest request) {

        try {
            PoliticaInventarioResult politica = gestionarPoliticaUseCase.actualizar(toCommand(id, empresaId, request));
            return ResponseEntity.ok(toResponse(politica));
        } catch (PoliticaInventarioNoEncontradaException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }

    // -------------------------------------------------------------------------
    // Mapper privado: Dominio → Web Response DTO
    // -------------------------------------------------------------------------
    private GestionarPoliticaInventarioCommand toCommand(
            UUID politicaId, UUID empresaId, PoliticaInventarioRequest request) {
        return new GestionarPoliticaInventarioCommand(
                politicaId,
                empresaId,
                request.bodegaId(),
                request.productoId(),
                request.puntoReorden(),
                request.nivelOptimo(),
                request.activa()
        );
    }

    private PoliticaInventarioResponse toResponse(PoliticaInventarioResult p) {
        return new PoliticaInventarioResponse(
                p.id(),
                p.empresaId(),
                p.bodegaId(),
                p.productoId(),
                p.puntoReorden(),
                p.nivelOptimo(),
                p.activa(),
                null, // creadoEn — manejado por AuditableJpaEntity
                null  // actualizadoEn — manejado por AuditableJpaEntity
        );
    }
}
