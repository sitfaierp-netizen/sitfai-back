package com.SITFAI_CORE_ERP_TIENDA.orders.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.orders.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CrearPedidoService {

    private final PedidoRepository pedidoRepository;
    private final ActorProviderPort actorProviderPort;

    public CrearPedidoService(PedidoRepository pedidoRepository, ActorProviderPort actorProviderPort) {
        this.pedidoRepository = pedidoRepository;
        this.actorProviderPort = actorProviderPort;
    }

    public UUID ejecutar(CrearPedidoCommand command) {
        String createdBy = actorProviderPort.getCurrentActorId();
        
        Pedido pedido = Pedido.crear(
                new PedidoId(UUID.randomUUID()),
                command.empresaId(),
                new ClienteId(command.clienteId()),
                createdBy
        );

        for (CrearPedidoCommand.LineaComando linea : command.lineas()) {
            pedido.agregarLinea(
                    new ProductoId(linea.productoId()),
                    linea.cantidad(),
                    new Dinero(linea.precioUnitario())
            );
        }

        pedidoRepository.save(pedido);
        return pedido.getId().value();
    }
}
