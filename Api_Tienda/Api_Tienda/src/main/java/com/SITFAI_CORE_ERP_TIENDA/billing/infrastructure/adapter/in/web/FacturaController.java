package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.ConsultarFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto.FacturaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.mapper.FacturaWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/billing/facturas")
public class FacturaController {

    private final EmitirFacturaUseCase emitirFacturaUseCase;
    private final ConsultarFacturaUseCase consultarFacturaUseCase;

    public FacturaController(EmitirFacturaUseCase emitirFacturaUseCase, ConsultarFacturaUseCase consultarFacturaUseCase) {
        this.emitirFacturaUseCase = emitirFacturaUseCase;
        this.consultarFacturaUseCase = consultarFacturaUseCase;
    }

    /**
     * Endpoint para emitir una Factura Electrónica.
     * Zero Trust: Inyecta el empresa_id del JWT. No lo pide en el JSON.
     */
    @PostMapping
    @PreAuthorize("hasRole('FACTURADOR') or hasRole('EMPRESA_ADMIN')")
    public ResponseEntity<FacturaResponse> emitirFactura(
            @RequestBody FacturaWebRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID empresaId = UUID.fromString(jwt.getClaimAsString("empresa_id"));
        
        EmitirFacturaCommand command = FacturaWebMapper.toCommand(empresaId, request);
        FacturaResponse response = emitirFacturaUseCase.emitirFactura(command);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para consultar una Factura Electrónica.
     * Zero Trust: Filtra siempre por el empresa_id del JWT.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('FACTURADOR') or hasRole('EMPRESA_ADMIN') or hasRole('AUDITOR')")
    public ResponseEntity<FacturaResponse> consultarFactura(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
            
        UUID empresaId = UUID.fromString(jwt.getClaimAsString("empresa_id"));
        
        FacturaResponse response = consultarFacturaUseCase.consultarPorId(new FacturaId(id), new EmpresaId(empresaId));
        return ResponseEntity.ok(response);
    }
}
