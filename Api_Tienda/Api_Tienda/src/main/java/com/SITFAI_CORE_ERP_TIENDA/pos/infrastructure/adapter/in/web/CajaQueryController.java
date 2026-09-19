package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.ConsultarCajasUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.query.dto.CajaView;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cajas")
public class CajaQueryController {

    private final ConsultarCajasUseCase consultarCajasUseCase;

    public CajaQueryController(ConsultarCajasUseCase consultarCajasUseCase) {
        this.consultarCajasUseCase = consultarCajasUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CAJERO', 'ADMIN_BODEGA')")
    public ResponseEntity<List<CajaView>> listarCajas(@TenantId UUID empresaId) {
        List<CajaView> cajas = consultarCajasUseCase.listarCajasPorEmpresa(empresaId);
        return ResponseEntity.ok(cajas);
    }
}
