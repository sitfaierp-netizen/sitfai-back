package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ListarBodegasCQRSUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto.BodegaView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bodegas")
public class BodegaQueryController {

    private final ListarBodegasCQRSUseCase listarBodegasCQRSUseCase;

    public BodegaQueryController(ListarBodegasCQRSUseCase listarBodegasCQRSUseCase) {
        this.listarBodegasCQRSUseCase = listarBodegasCQRSUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPRESA_ADMIN', 'BODEGA_OPERATOR')")
    public ResponseEntity<List<BodegaView>> listarBodegas(@com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId java.util.UUID empresaId) {
        List<BodegaView> bodegas = listarBodegasCQRSUseCase.listarBodegasPorEmpresa(empresaId);
        return ResponseEntity.ok(bodegas);
    }
}
