package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Event: PedidoConfirmadoEvent.
 * <p>
 * Se emite cuando el Agregado {@link com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido}
 * pasa al estado {@code CONFIRMADO}.
 * <p>
 * Encapsula el tenant raíz (MT-01), el ID del pedido y la lista inmutable de líneas de productos
 * con sus cantidades para que el Bounded Context de Inventario reserve el stock correspondiente.
 * <p>
 * Regla REGLA-3: Evento inmutable (record Java 21) sin frameworks.
 */
public record PedidoConfirmadoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        PedidoId pedidoId,
        ClienteId clienteId,
        Dinero total,
        List<LineaPedido> lineas
) implements DomainEvent {
    public PedidoConfirmadoEvent {
        Objects.requireNonNull(empresaId, "PedidoConfirmadoEvent: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(pedidoId,  "PedidoConfirmadoEvent: pedidoId es obligatorio.");
        Objects.requireNonNull(clienteId, "PedidoConfirmadoEvent: clienteId es obligatorio.");
        Objects.requireNonNull(total,     "PedidoConfirmadoEvent: total es obligatorio.");
        Objects.requireNonNull(lineas,    "PedidoConfirmadoEvent: lineas es obligatorio.");
        if (lineas.isEmpty()) {
            throw new IllegalArgumentException("PedidoConfirmadoEvent: debe contener al menos una línea.");
        }
        if (eventoId == null)   eventoId   = UUID.randomUUID();
        if (ocurridoEn == null) ocurridoEn = Instant.now();
        lineas = List.copyOf(lineas);
    }

    public static PedidoConfirmadoEvent of(
            EmpresaId empresaId,
            PedidoId pedidoId,
            ClienteId clienteId,
            Dinero total,
            List<LineaPedido> lineas) {
        return new PedidoConfirmadoEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                pedidoId,
                clienteId,
                total,
                lineas
        );
    }
}
