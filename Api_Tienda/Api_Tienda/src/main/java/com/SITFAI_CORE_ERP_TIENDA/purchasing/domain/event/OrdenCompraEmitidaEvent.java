package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import java.util.UUID;
import java.time.Instant;

public record OrdenCompraEmitidaEvent(
        UUID empresaId,
        OrdenCompraId ordenCompraId,
        Instant ocurridoEn
) implements DomainEvent {
    public static OrdenCompraEmitidaEvent ahora(UUID empresaId, OrdenCompraId ordenCompraId) {
        return new OrdenCompraEmitidaEvent(empresaId, ordenCompraId, Instant.now());
    }
}
