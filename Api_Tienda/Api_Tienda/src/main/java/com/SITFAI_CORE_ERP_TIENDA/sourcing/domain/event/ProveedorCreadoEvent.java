package com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.event;

import java.util.UUID;

/**
 * Domain Event: emitido cuando un Proveedor es creado exitosamente en Sourcing.
 * Record inmutable — listo para consumo por otros Bounded Contexts.
 */
public record ProveedorCreadoEvent(
        UUID proveedorId,
        UUID empresaId,
        String ruc,
        String razonSocial
) implements DomainEvent {

    public static ProveedorCreadoEvent of(UUID proveedorId, UUID empresaId,
                                          String ruc, String razonSocial) {
        return new ProveedorCreadoEvent(proveedorId, empresaId, ruc, razonSocial);
    }
}
