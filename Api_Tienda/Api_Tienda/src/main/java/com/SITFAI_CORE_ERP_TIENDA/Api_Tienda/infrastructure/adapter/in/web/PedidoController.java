package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.GestionarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CrearPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.PedidoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.mapper.PedidoWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Driving Adapter: Controlador REST para la gestión de Pedidos Comerciales en Api_Tienda.
 * <p>
 * Expone las rutas canónicas relativas /pedidos (POST) y /pedidos/{id}/confirmar (PATCH) (Regla 5).
 * Seguridad Zero Trust: Extracción segura de tenant (MT-01, MT-02).
 */
@RestController("apiTiendaPedidoController")
@RequestMapping("/pedidos")
public class PedidoController {

    private final GestionarPedidoUseCase gestionarPedidoUseCase;
    private final TenantProviderPort tenantProviderPort;
    private final PedidoWebMapper pedidoWebMapper;

    public PedidoController(
            GestionarPedidoUseCase gestionarPedidoUseCase,
            TenantProviderPort tenantProviderPort,
            PedidoWebMapper pedidoWebMapper) {
        this.gestionarPedidoUseCase = Objects.requireNonNull(gestionarPedidoUseCase, "GestionarPedidoUseCase no puede ser nulo");
        this.tenantProviderPort = tenantProviderPort;
        this.pedidoWebMapper = Objects.requireNonNull(pedidoWebMapper, "PedidoWebMapper no puede ser nulo");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoWebResponse crearPedido(@RequestBody CrearPedidoWebRequest request) {
        List<CrearPedidoCommand.LineaComando> lineasComando = request.lineas().stream()
                .map(l -> new CrearPedidoCommand.LineaComando(l.productoId(), l.cantidad(), l.precioUnitario()))
                .toList();

        UUID tenantId = tenantProviderPort != null && tenantProviderPort.getEmpresaIdAutenticada() != null
                ? tenantProviderPort.getEmpresaIdAutenticada().valor()
                : null;

        CrearPedidoCommand command = new CrearPedidoCommand(
                tenantId,
                request.clienteId(),
                lineasComando
        );

        PedidoResponse response = gestionarPedidoUseCase.crear(command);
        return pedidoWebMapper.toWebResponse(response);
    }

    @PatchMapping("/{id}/confirmar")
    @ResponseStatus(HttpStatus.OK)
    public PedidoWebResponse confirmarPedido(@PathVariable("id") UUID id) {
        UUID tenantId = tenantProviderPort != null && tenantProviderPort.getEmpresaIdAutenticada() != null
                ? tenantProviderPort.getEmpresaIdAutenticada().valor()
                : null;

        ConfirmarPedidoCommand command = new ConfirmarPedidoCommand(tenantId, id);
        PedidoResponse response = gestionarPedidoUseCase.confirmar(command);
        return pedidoWebMapper.toWebResponse(response);
    }
}
