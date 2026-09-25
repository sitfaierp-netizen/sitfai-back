package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;

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

    public record ItemPedido(UUID id, ProductoId productoId, Cantidad cantidad, Dinero precioUnitario) {
        public UUID getId() { return id; }
        public ProductoId getProductoId() { return productoId; }
        public Cantidad getCantidad() { return cantidad; }
        public Dinero getPrecioUnitario() { return precioUnitario; }
    }

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

        List<ItemPedido> items = lineas.stream().map(l -> new ItemPedido(
                l.getId(),
                new ProductoId(l.getProductoId().valor()),
                Cantidad.de(l.getCantidad()),
                Dinero.de(l.getPrecioUnitario().monto())
        )).toList();

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

