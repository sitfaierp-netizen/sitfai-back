package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.CompletarPackingUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.ConsultarDespachoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.PlanificarDespachoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input.RegistrarPickingUseCase;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web.dto.PlanificarDespachoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web.dto.RegistrarPickingWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/fulfillment/despachos")
public class OrdenDespachoController {

    private final PlanificarDespachoUseCase planificarUseCase;
    private final RegistrarPickingUseCase registrarPickingUseCase;
    private final CompletarPackingUseCase completarPackingUseCase;
    private final ConsultarDespachoUseCase consultarUseCase;

    public OrdenDespachoController(
            PlanificarDespachoUseCase planificarUseCase,
            RegistrarPickingUseCase registrarPickingUseCase,
            CompletarPackingUseCase completarPackingUseCase,
            ConsultarDespachoUseCase consultarUseCase) {
        this.planificarUseCase = Objects.requireNonNull(planificarUseCase);
        this.registrarPickingUseCase = Objects.requireNonNull(registrarPickingUseCase);
        this.completarPackingUseCase = Objects.requireNonNull(completarPackingUseCase);
        this.consultarUseCase = Objects.requireNonNull(consultarUseCase);
    }

    @PostMapping
    public ResponseEntity<OrdenDespachoResponse> planificar(
            @TenantId UUID empresaId,
            @RequestBody PlanificarDespachoWebRequest request) {

        var command = OrdenDespachoWebMapper.toCommand(empresaId, request);
        var response = planificarUseCase.ejecutar(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/picking")
    public ResponseEntity<OrdenDespachoResponse> registrarPicking(
            @TenantId UUID empresaId,
            @PathVariable UUID id,
            @RequestBody RegistrarPickingWebRequest request) {

        var command = OrdenDespachoWebMapper.toCommand(empresaId, id, request);
        var response = registrarPickingUseCase.ejecutar(command);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/completar")
    public ResponseEntity<OrdenDespachoResponse> completarPacking(
            @TenantId UUID empresaId,
            @PathVariable UUID id) {

        var command = OrdenDespachoWebMapper.toCompletarCommand(empresaId, id);
        var response = completarPackingUseCase.ejecutar(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenDespachoResponse> consultar(
            @TenantId UUID empresaId,
            @PathVariable UUID id) {

        var response = consultarUseCase.porId(id, empresaId);
        return ResponseEntity.ok(response);
    }
}
