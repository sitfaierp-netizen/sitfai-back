package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de Integración emitido cuando un pedido es cancelado.
 * Especialmente utilizado en la SAGA para compensaciones por falta de stock.
 * <p>
 * Reglas validadas: REGLA-3 (Domain Events inmutables).
 */
public record PedidoCanceladoEvent(
        UUID eventoId,
        Instant ocurridoEn,
        PedidoId pedidoId,
        EmpresaId empresaId,
        ClienteId clienteId,
        String motivo
) implements DomainEvent {

    public static PedidoCanceladoEvent of(PedidoId pedidoId, EmpresaId empresaId, ClienteId clienteId, String motivo) {
        return new PedidoCanceladoEvent(
                UUID.randomUUID(),
                Instant.now(),
                pedidoId,
                empresaId,
                clienteId,
                motivo
        );
    }
}
