package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.NivelOptimo;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PuntoReorden;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output.PoliticaInventarioRepository;
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

    private final PoliticaInventarioRepository politicaRepository;

    public PoliticaInventarioController(PoliticaInventarioRepository politicaRepository) {
        this.politicaRepository = politicaRepository;
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

        PoliticaInventario politica = new PoliticaInventario(
                new PoliticaId(UUID.randomUUID()),
                new EmpresaId(empresaId),
                new BodegaId(request.bodegaId()),
                new ProductoId(request.productoId()),
                new PuntoReorden(request.puntoReorden()),
                new NivelOptimo(request.nivelOptimo()),
                request.activa()
        );

        politicaRepository.guardar(politica);
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

        PoliticaInventario politicaExistente = politicaRepository
                .buscarPorId(new PoliticaId(id), new EmpresaId(empresaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Política de inventario no encontrada: " + id));

        PoliticaInventario politicaActualizada = new PoliticaInventario(
                politicaExistente.getId(),
                politicaExistente.getEmpresaId(),
                new BodegaId(request.bodegaId()),
                new ProductoId(request.productoId()),
                new PuntoReorden(request.puntoReorden()),
                new NivelOptimo(request.nivelOptimo()),
                request.activa()
        );

        politicaRepository.guardar(politicaActualizada);
        return ResponseEntity.ok(toResponse(politicaActualizada));
    }

    // -------------------------------------------------------------------------
    // Mapper privado: Dominio → Web Response DTO
    // -------------------------------------------------------------------------
    private PoliticaInventarioResponse toResponse(PoliticaInventario p) {
        return new PoliticaInventarioResponse(
                p.getId().valor(),
                p.getEmpresaId().valor(),
                p.getBodegaId().valor(),
                p.getProductoId().valor(),
                p.getPuntoReorden().valor(),
                p.getNivelOptimo().valor(),
                p.isActiva(),
                null, // creadoEn — manejado por AuditableJpaEntity
                null  // actualizadoEn — manejado por AuditableJpaEntity
        );
    }
}
