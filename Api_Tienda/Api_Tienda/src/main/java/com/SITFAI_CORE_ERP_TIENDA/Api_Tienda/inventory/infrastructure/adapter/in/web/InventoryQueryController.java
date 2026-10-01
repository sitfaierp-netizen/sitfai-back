package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ConsultarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.MovimientoKardexView;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.StockDisponibleView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller para las consultas (Read Model) del módulo de Inventario.
 * Aislado de las operaciones de comando.
 */
@RestController
@RequestMapping("/inventory")
public class InventoryQueryController {

    private final ConsultarStockUseCase consultarStockUseCase;

    public InventoryQueryController(ConsultarStockUseCase consultarStockUseCase) {
        this.consultarStockUseCase = consultarStockUseCase;
    }

    /**
     * Obtiene el stock disponible de todos los productos en una bodega.
     * MT-02: empresaId no se pasa por parámetro, lo resuelve el caso de uso.
     */
    @GetMapping("/bodegas/{bodegaId}/stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN', 'BODEGA_OPERATOR')")
    public ResponseEntity<List<StockDisponibleView>> getStockByBodega(@PathVariable String bodegaId) {
        List<StockDisponibleView> stock = consultarStockUseCase.consultarStockBodega(bodegaId);
        return ResponseEntity.ok(stock);
    }

    /**
     * Obtiene el kárdex (movimientos) de un producto específico en una bodega.
     * MT-02: empresaId no se pasa por parámetro, lo resuelve el caso de uso.
     */
    @GetMapping("/bodegas/{bodegaId}/productos/{productoId}/kardex")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN', 'BODEGA_OPERATOR')")
    public ResponseEntity<List<MovimientoKardexView>> getKardex(
            @PathVariable String bodegaId,
            @PathVariable String productoId) {
        List<MovimientoKardexView> kardex = consultarStockUseCase.consultarKardex(bodegaId, productoId);
        return ResponseEntity.ok(kardex);
    }
}
