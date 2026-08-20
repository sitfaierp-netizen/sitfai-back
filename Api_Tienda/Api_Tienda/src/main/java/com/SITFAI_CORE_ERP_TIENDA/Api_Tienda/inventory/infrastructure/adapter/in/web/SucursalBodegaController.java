package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.ObtenerBodegasPorSucursalUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sucursales")
public class SucursalBodegaController {

    private final ObtenerBodegasPorSucursalUseCase obtenerBodegasPorSucursalUseCase;

    public SucursalBodegaController(ObtenerBodegasPorSucursalUseCase obtenerBodegasPorSucursalUseCase) {
        this.obtenerBodegasPorSucursalUseCase = obtenerBodegasPorSucursalUseCase;
    }

    @GetMapping("/{sucursalId}/bodegas")
    public ResponseEntity<List<BodegaResponse>> obtenerBodegasPorSucursal(@PathVariable String sucursalId) {
        List<BodegaResponse> bodegas = obtenerBodegasPorSucursalUseCase.ejecutar(sucursalId);
        return ResponseEntity.ok(bodegas);
    }
}
