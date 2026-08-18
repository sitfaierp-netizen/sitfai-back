package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.StockQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.ConsultarStockConsolidadoQuery;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.StockConsolidadoView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory/reports")
public class InventoryReportController {

    private final StockQueryRepository queryRepository;

    public InventoryReportController(StockQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    /**
     * Endpoint de solo lectura (CQRS) para consultar el stock consolidado.
     * ZERO TRUST: Extrae el empresa_id del token JWT para asegurar el aislamiento (MT-01, MT-04).
     */
    @GetMapping("/stock-consolidado")
    @PreAuthorize("hasRole('BODEGA_OPERATOR') or hasRole('SUCURSAL_MANAGER') or hasRole('EMPRESA_ADMIN')")
    public ResponseEntity<List<StockConsolidadoView>> getStockConsolidado(
            @RequestParam UUID bodegaId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID empresaId = UUID.fromString(jwt.getClaimAsString("empresa_id"));

        ConsultarStockConsolidadoQuery query = new ConsultarStockConsolidadoQuery(empresaId, bodegaId, null);
        List<StockConsolidadoView> result = queryRepository.consultarStockConsolidado(query);

        return ResponseEntity.ok(result);
    }
}
