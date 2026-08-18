package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Emitido cuando una Orden de Compra es cancelada.
 */
public record OrdenCompraCanceladaEvent(
        UUID eventoId,
        OrdenCompraId ordenCompraId,
        EmpresaId empresaId,
        String motivo,
        Instant ocurridoEn
) implements DomainEvent {

    public OrdenCompraCanceladaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null");
        Objects.requireNonNull(ordenCompraId, "ordenCompraId no puede ser null");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null");
        Objects.requireNonNull(motivo, "motivo no puede ser null");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null");
    }

    public static OrdenCompraCanceladaEvent ahora(OrdenCompraId ordenCompraId, EmpresaId empresaId, String motivo) {
        return new OrdenCompraCanceladaEvent(UUID.randomUUID(), ordenCompraId, empresaId, motivo, Instant.now());
    }
}
