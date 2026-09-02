package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import java.util.UUID;
import java.time.Instant;

public record OrdenCompraCreadaEvent(
        UUID empresaId,
        OrdenCompraId ordenCompraId,
        Instant ocurridoEn
) implements DomainEvent {
    public static OrdenCompraCreadaEvent ahora(UUID empresaId, OrdenCompraId ordenCompraId) {
        return new OrdenCompraCreadaEvent(empresaId, ordenCompraId, Instant.now());
    }
}
