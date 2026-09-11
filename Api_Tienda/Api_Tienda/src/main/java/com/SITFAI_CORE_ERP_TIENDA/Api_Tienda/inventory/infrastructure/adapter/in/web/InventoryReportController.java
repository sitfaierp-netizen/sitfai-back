package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.StockQueryRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.ConsultarStockConsolidadoQuery;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.StockConsolidadoView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory/reports")
public class InventoryReportController {

    private final StockQueryRepository queryRepository;

    public InventoryReportController(StockQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @GetMapping("/stock-consolidado")
    @PreAuthorize(
        "hasRole('SUPER_ADMIN') or hasRole('EMPRESA_ADMIN') or " +
        "hasRole('BODEGA_OPERATOR') or hasRole('SUCURSAL_MANAGER')")
    public ResponseEntity<List<StockConsolidadoView>> getStockConsolidado(
            @RequestParam UUID bodegaId,
            @AuthenticationPrincipal Jwt jwt) {

        String empresaIdStr = jwt.getClaimAsString("empresa_id");
        if (empresaIdStr == null || empresaIdStr.isBlank()) {
            empresaIdStr = jwt.getClaimAsString("empresaId");
        }
        if (empresaIdStr == null || empresaIdStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JWT sin claim empresa_id. Configure Protocol Mapper en Keycloak.");
        }
        UUID empresaId;
        try {
            empresaId = UUID.fromString(empresaIdStr.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "empresa_id invalido: " + empresaIdStr);
        }
        ConsultarStockConsolidadoQuery query = new ConsultarStockConsolidadoQuery(empresaId, bodegaId, null);
        return ResponseEntity.ok(queryRepository.consultarStockConsolidado(query));
    }
}
