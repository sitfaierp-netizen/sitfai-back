package com.SITFAI_CORE_ERP_TIENDA.orders.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.orders.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("ordersConfirmarPedidoService")
@Transactional
public class ConfirmarPedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;
    private final ActorProviderPort actorProviderPort;

    public ConfirmarPedidoService(PedidoRepository pedidoRepository, PedidoEventPublisher eventPublisher, ActorProviderPort actorProviderPort) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
        this.actorProviderPort = actorProviderPort;
    }

    public void ejecutar(ConfirmarPedidoCommand command) {
        Pedido pedido = pedidoRepository.findByIdAndEmpresaId(new PedidoId(command.pedidoId()), command.empresaId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado o no pertenece a la empresa actual"));
        
        // Ejecutar acción de dominio
        pedido.confirmar();
        
        // Esto asume que el adapter va a actualizar la entidad a nivel JPA
        // Pero para el patrón Audit, podemos usar un setUpdatedBy en la Entidad Jpa o si el Agregado lo soporta
        // En nuestro caso el Agregado es un POJO pero en su constructor no tenemos setUpdatedBy, no es necesario,
        // lo controlamos en el Mapper o adapter antes de guardar. 
        // Aunque el dominio de Pedido podría tener los setters privados si quisiéramos.
        // Pero al confirmar(), la fecha updateable la puede setear Hibernate.
        // En este paso delegamos el guardado al repo.
        pedidoRepository.save(pedido);

        // Publicar eventos
        for (DomainEvent event : pedido.pullDomainEvents()) {
            eventPublisher.publish(event);
        }
    }
}
