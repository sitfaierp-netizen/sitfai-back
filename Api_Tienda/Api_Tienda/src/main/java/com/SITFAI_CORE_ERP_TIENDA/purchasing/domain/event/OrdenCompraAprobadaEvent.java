package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;

import java.time.Instant;

public record OrdenCompraAprobadaEvent(
        String eventId,
        Instant occurredOn,
        OrdenCompraId ordenCompraId,
        EmpresaId empresaId
) {
    public OrdenCompraAprobadaEvent(OrdenCompraId ordenCompraId, EmpresaId empresaId) {
        this(java.util.UUID.randomUUID().toString(), Instant.now(), ordenCompraId, empresaId);
    }
}
