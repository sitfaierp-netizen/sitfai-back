package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain Event: Emitido cuando un {@link com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido} es confirmado.
 * <p>
 * Notifica a otros Bounded Contexts (ej. Inventario para reservar/descontar stock,
 * Facturación para emitir comprobante, etc.).
 * <p>
 * Inmutable por diseño (record Java 25).
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
