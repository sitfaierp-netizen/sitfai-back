package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CrearPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CrearPedidoWebRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController("apiTiendaPedidoController")
@RequestMapping("/api-tienda/pedidos")
public class PedidoController {

    private final CrearPedidoUseCase crearPedidoUseCase;
    private final TenantProviderPort tenantProviderPort;

    public PedidoController(CrearPedidoUseCase crearPedidoUseCase, TenantProviderPort tenantProviderPort) {
        this.crearPedidoUseCase = crearPedidoUseCase;
        this.tenantProviderPort = tenantProviderPort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse crearPedido(@RequestBody CrearPedidoWebRequest request) {
        List<CrearPedidoCommand.LineaComando> lineasComando = request.lineas().stream()
                .map(l -> new CrearPedidoCommand.LineaComando(l.productoId(), l.cantidad(), l.precioUnitario()))
                .collect(Collectors.toList());

        CrearPedidoCommand command = new CrearPedidoCommand(
                tenantProviderPort.getEmpresaIdAutenticada().valor(),
                request.clienteId(),
                lineasComando
        );

        return crearPedidoUseCase.ejecutar(command);
    }
}
