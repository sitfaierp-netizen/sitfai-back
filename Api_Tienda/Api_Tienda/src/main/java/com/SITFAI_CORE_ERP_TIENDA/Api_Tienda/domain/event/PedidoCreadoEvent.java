package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.ItemPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio emitido cuando un Pedido solicita la reserva de stock.
 */
public record PedidoCreadoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        PedidoId pedidoId,
        EmpresaId empresaId,
        ClienteId clienteId,
        Dinero total,
        List<ItemPedido> items
) implements DomainEvent {

    public PedidoCreadoEvent {
        Objects.requireNonNull(eventoId);
        Objects.requireNonNull(ocurridoEn);
        Objects.requireNonNull(pedidoId);
        Objects.requireNonNull(empresaId);
        Objects.requireNonNull(clienteId);
        Objects.requireNonNull(total);
        Objects.requireNonNull(items);
        items = List.copyOf(items); // inmutable
    }

    public static PedidoCreadoEvent of(
            PedidoId pedidoId,
            EmpresaId empresaId,
            ClienteId clienteId,
            Dinero total,
            List<LineaPedido> lineas) {

        List<ItemPedido> items = lineas.stream().map(LineaPedido::aItemPedido).toList();
        return new PedidoCreadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                pedidoId,
                empresaId,
                clienteId,
                total,
                items
        );
    }
}
