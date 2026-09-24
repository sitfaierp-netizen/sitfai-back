package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.in.web.Idempotent;
import com.SITFAI_CORE_ERP_TIENDA.orders.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.orders.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.orders.application.service.ConfirmarPedidoService;
import com.SITFAI_CORE_ERP_TIENDA.orders.application.service.CrearPedidoService;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.in.web.dto.CrearPedidoRequest;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders/pedidos")
public class PedidoController {

    private final CrearPedidoService crearPedidoService;
    private final ConfirmarPedidoService confirmarPedidoService;

    public PedidoController(CrearPedidoService crearPedidoService, ConfirmarPedidoService confirmarPedidoService) {
        this.crearPedidoService = crearPedidoService;
        this.confirmarPedidoService = confirmarPedidoService;
    }

    @Idempotent
    @PostMapping
    public ResponseEntity<Void> crearPedido(
            @TenantId UUID empresaId,
            @RequestBody CrearPedidoRequest request
    ) {
        List<CrearPedidoCommand.LineaComando> lineas = request.lineas().stream()
                .map(l -> new CrearPedidoCommand.LineaComando(l.productoId(), l.cantidad(), l.precioUnitario()))
                .toList();

        CrearPedidoCommand command = new CrearPedidoCommand(
                empresaId,
                request.clienteId(),
                lineas
        );

        UUID pedidoId = crearPedidoService.ejecutar(command);

        return ResponseEntity.created(URI.create("/pedidos/" + pedidoId)).build();
    }

    @Idempotent
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmarPedido(
            @TenantId UUID empresaId,
            @PathVariable UUID id
    ) {
        ConfirmarPedidoCommand command = new ConfirmarPedidoCommand(id, empresaId);
        confirmarPedidoService.ejecutar(command);

        return ResponseEntity.noContent().build();
    }
}
