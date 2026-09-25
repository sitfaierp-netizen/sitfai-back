package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain Event: Emitido cuando un {@link com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido} es confirmado.
 * <p>
 * Notifica a otros Bounded Contexts (ej. Inventario para reservar/descontar stock,
 * FacturaciÃ³n para emitir comprobante, etc.).
 * <p>
 * Inmutable por diseÃ±o (record Java 25).
 */
public record PedidoConfirmadoEvent(
        UUID eventoId,
        PedidoId pedidoId,
        EmpresaId empresaId,
        ClienteId clienteId,
        Dinero total,
        List<LineaPedido> lineas,
        Instant ocurridoEn
) implements DomainEvent {

    public static PedidoConfirmadoEvent of(
            PedidoId pedidoId,
            EmpresaId empresaId,
            ClienteId clienteId,
            Dinero total,
            List<LineaPedido> lineas) {

        return new PedidoConfirmadoEvent(
                UUID.randomUUID(),
                pedidoId,
                empresaId,
                clienteId,
                total,
                List.copyOf(lineas),
                Instant.now()
        );
    }
}

