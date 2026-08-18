package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProveedorId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio: Emitido cuando se crea una nueva Orden de Compra en estado BORRADOR.
 */
public record OrdenCompraCreadaEvent(
        UUID eventoId,
        OrdenCompraId ordenCompraId,
        EmpresaId empresaId,
        ProveedorId proveedorId,
        Instant ocurridoEn
) implements DomainEvent {

    public OrdenCompraCreadaEvent {
        Objects.requireNonNull(eventoId, "eventoId no puede ser null");
        Objects.requireNonNull(ordenCompraId, "ordenCompraId no puede ser null");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null");
        Objects.requireNonNull(proveedorId, "proveedorId no puede ser null");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn no puede ser null");
    }

    public static OrdenCompraCreadaEvent ahora(OrdenCompraId ordenCompraId, EmpresaId empresaId, ProveedorId proveedorId) {
        return new OrdenCompraCreadaEvent(UUID.randomUUID(), ordenCompraId, empresaId, proveedorId, Instant.now());
    }
}
