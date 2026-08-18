package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.PedidoOrigenId;

import java.time.Instant;
import java.util.UUID;

public record PackingCompletadoEvent(
        UUID eventoId,
        DespachoId despachoId,
        EmpresaId empresaId,
        PedidoOrigenId pedidoOrigenId,
        Instant ocurridoEn
) implements DomainEvent {
    public static PackingCompletadoEvent of(DespachoId despachoId, EmpresaId empresaId, PedidoOrigenId pedidoOrigenId) {
        return new PackingCompletadoEvent(
                UUID.randomUUID(),
                despachoId,
                empresaId,
                pedidoOrigenId,
                Instant.now()
        );
    }
}
