package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.LineaPedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.GestionarPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.port.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application Service: Implementa {@link GestionarPedidoUseCase} orquestando la creación
 * y confirmación del Agregado {@link Pedido}.
 * <p>
 * Regla MT-01 / MT-02: Garantiza extracción segura de empresa_id desde {@link TenantProviderPort}.
 * Regla REGLA-1: Orquestación pura sin lógica de negocio en la capa de aplicación.
 */
@Service("gestionarPedidoService")
public class GestionarPedidoService implements GestionarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final TenantProviderPort tenantProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public GestionarPedidoService(
            PedidoRepository pedidoRepository,
            TenantProviderPort tenantProviderPort,
            ApplicationEventPublisher eventPublisher) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository, "PedidoRepository no puede ser nulo");
        this.tenantProviderPort = tenantProviderPort;
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher no puede ser nulo");
    }

    @Override
    @Transactional
    public PedidoResponse crear(CrearPedidoCommand command) {
        Objects.requireNonNull(command, "CrearPedidoCommand no puede ser nulo");

        EmpresaId empresaId = resolverEmpresaId(command.empresaId());
        ClienteId clienteId = new ClienteId(command.clienteId());

        Pedido pedido = Pedido.crear(empresaId, clienteId);

        for (CrearPedidoCommand.LineaComando linea : command.lineas()) {
            pedido.agregarLinea(
                    new ProductoId(linea.productoId()),
                    linea.cantidad(),
                    Dinero.de(linea.precioUnitario())
            );
        }

        pedidoRepository.guardar(pedido);
        return toResponse(pedido);
    }

    @Override
    @Transactional
    public PedidoResponse confirmar(ConfirmarPedidoCommand command) {
        Objects.requireNonNull(command, "ConfirmarPedidoCommand no puede ser nulo");

        EmpresaId empresaId = resolverEmpresaId(command.empresaId());
        PedidoId pedidoId = new PedidoId(command.pedidoId());

        Pedido pedido = pedidoRepository.buscarPorId(empresaId, pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + command.pedidoId()));

        pedido.confirmar();
        pedidoRepository.guardar(pedido);

        // Despachar eventos de dominio acumulados hacia el contexto de Spring (Coreografía con Inventario)
        List<com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent> eventos = pedido.pullDomainEvents();
        for (com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent evento : eventos) {
            eventPublisher.publishEvent(evento);
        }

        return toResponse(pedido);
    }

    private EmpresaId resolverEmpresaId(UUID empresaIdComando) {
        if (empresaIdComando != null) {
            return new EmpresaId(empresaIdComando);
        }
        if (tenantProviderPort != null && tenantProviderPort.getEmpresaIdAutenticada() != null) {
            return new EmpresaId(tenantProviderPort.getEmpresaIdAutenticada().valor());
        }
        throw new IllegalStateException("MT-01: No se pudo resolver el tenant empresa_id desde el contexto de seguridad.");
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<LineaPedidoResponse> lineasResp = pedido.getLineas().stream()
                .map(this::toLineaResponse)
                .toList();

        return new PedidoResponse(
                pedido.getId().valor(),
                pedido.getEmpresaId().valor(),
                pedido.getClienteId().valor(),
                pedido.getEstado().name(),
                pedido.calcularTotal().monto(),
                "USD",
                lineasResp,
                pedido.getCreadoEn(),
                pedido.getActualizadoEn()
        );
    }

    private LineaPedidoResponse toLineaResponse(LineaPedido linea) {
        return new LineaPedidoResponse(
                UUID.randomUUID(),
                linea.getProductoId().valor(),
                linea.getCantidad(),
                linea.getPrecioUnitario().monto(),
                "USD",
                linea.calcularSubtotal().monto()
        );
    }
}
