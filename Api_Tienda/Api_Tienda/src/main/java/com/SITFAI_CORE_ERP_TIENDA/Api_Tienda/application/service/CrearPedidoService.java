package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input.CrearPedidoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;

import org.springframework.stereotype.Service;

import java.util.List;

@Service("ecommerceCrearPedidoService")
public class CrearPedidoService implements CrearPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;
    private final TenantProviderPort tenantProviderPort;
    private final CurrentActorProvider currentActorProvider;

    public CrearPedidoService(
            PedidoRepository pedidoRepository,
            PedidoEventPublisher eventPublisher,
            TenantProviderPort tenantProviderPort,
            CurrentActorProvider currentActorProvider) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
        this.tenantProviderPort = tenantProviderPort;
        this.currentActorProvider = currentActorProvider;
    }

    @Override
    public PedidoResponse ejecutar(CrearPedidoCommand command) {
        // MT-01: Extraer de puerto (confianza cero)
        EmpresaId empresaId = tenantProviderPort.getEmpresaIdAutenticada();
        
        // El actor puede ser usado para trazas o auditoría, pero no en dominio.
        String actor = currentActorProvider.getActorActual();

        Pedido pedido = Pedido.iniciar(empresaId, ClienteId.de(command.clienteId()));

        for (CrearPedidoCommand.LineaComando linea : command.lineas()) {
            pedido.agregarItem(
                    ProductoId.de(linea.productoId()),
                    linea.cantidad(),
                    Dinero.de(linea.precioUnitario(), "USD") // asumiendo USD o de BD
            );
        }

        pedido.solicitarReserva();

        Pedido pedidoGuardado = pedidoRepository.guardar(pedido);

        List<DomainEvent> events = pedidoGuardado.drainDomainEvents();
        events.forEach(eventPublisher::publicar);

        List<com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.LineaPedidoResponse> lineasResp = pedidoGuardado.getLineas().stream()
                .map(l -> new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.LineaPedidoResponse(
                        l.getId(),
                        l.getProductoId().valor(),
                        l.getCantidad(),
                        l.getPrecioUnitario().monto(),
                        l.getPrecioUnitario().moneda(),
                        l.subtotal().monto()
                )).toList();

        return new PedidoResponse(
                pedidoGuardado.getId().valor(),
                pedidoGuardado.getEmpresaId().valor(),
                pedidoGuardado.getClienteId().valor(),
                pedidoGuardado.getEstado().name(),
                pedidoGuardado.calcularTotal().monto(),
                pedidoGuardado.calcularTotal().moneda(),
                lineasResp,
                pedidoGuardado.getCreadoEn(),
                pedidoGuardado.getActualizadoEn()
        );
    }
}
