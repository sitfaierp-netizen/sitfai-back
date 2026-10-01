package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.ObtenerBodegasPorSucursalUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Objects;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.mapper.InventarioWebMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.BodegaWebResponse;
import java.util.stream.Collectors;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/sucursales/{sucursalId}/bodegas")
public class SucursalBodegasController {

    private final ObtenerBodegasPorSucursalUseCase obtenerBodegasPorSucursalUseCase;
    private final InventarioWebMapper webMapper;

    public SucursalBodegasController(ObtenerBodegasPorSucursalUseCase obtenerBodegasPorSucursalUseCase, InventarioWebMapper webMapper) {
        this.obtenerBodegasPorSucursalUseCase = Objects.requireNonNull(obtenerBodegasPorSucursalUseCase);
        this.webMapper = Objects.requireNonNull(webMapper);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN', 'BODEGA_OPERATOR')")
    public ResponseEntity<List<BodegaWebResponse>> obtenerPorSucursal(
            @TenantId String empresaId,
            @PathVariable String sucursalId) {
        List<BodegaWebResponse> bodegas = obtenerBodegasPorSucursalUseCase.ejecutar(empresaId, sucursalId)
                .stream()
                .map(webMapper::toWebResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bodegas);
    }
}
