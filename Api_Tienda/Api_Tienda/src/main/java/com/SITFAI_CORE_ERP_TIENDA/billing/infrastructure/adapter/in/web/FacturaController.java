package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto.EmitirFacturaRequest;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.in.web.Idempotent;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/billing/facturas")
public class FacturaController {

    private final EmitirFacturaUseCase emitirFacturaUseCase;

    public FacturaController(EmitirFacturaUseCase emitirFacturaUseCase) {
        this.emitirFacturaUseCase = emitirFacturaUseCase;
    }

    @PostMapping
    @Idempotent
    public ResponseEntity<FacturaResponse> emitirFactura(
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @TenantId UUID empresaId, // Framework injected, zero trust
            @Valid @RequestBody EmitirFacturaRequest request) {

        EmitirFacturaCommand command = new EmitirFacturaCommand(
                empresaId,
                request.clienteId(),
                request.pedidoId(),
                request.rucCliente(),
                request.lineas().stream()
                        .map(l -> new EmitirFacturaCommand.LineaFacturaCommand(
                                l.concepto(),
                                l.cantidad(),
                                l.precioUnitario(),
                                l.moneda(),
                                l.impuestos() != null ? l.impuestos().stream()
                                        .map(i -> new EmitirFacturaCommand.ImpuestoCommand(i.tipo(), i.tarifa()))
                                        .collect(Collectors.toList()) : null
                        ))
                        .collect(Collectors.toList())
        );

        FacturaResponse response = emitirFacturaUseCase.emitirFactura(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
