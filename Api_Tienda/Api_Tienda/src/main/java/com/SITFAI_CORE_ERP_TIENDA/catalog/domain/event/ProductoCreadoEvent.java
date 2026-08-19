package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event: emitido cuando un Producto es creado exitosamente en el Catálogo.
 * Record inmutable — otros Bounded Contexts (inventory, Api_Tienda) pueden suscribirse.
 * Cero dependencias a frameworks (Regla 1, AUD-03).
 */
public record ProductoCreadoEvent(
        UUID productoId,
        UUID empresaId,
        String sku,
        String nombre,
        BigDecimal precioVenta,
        Instant ocurridoEn
) implements DomainEvent {

    public static ProductoCreadoEvent of(UUID productoId, UUID empresaId,
                                         String sku, String nombre,
                                         BigDecimal precioVenta) {
        return new ProductoCreadoEvent(productoId, empresaId, sku, nombre,
                precioVenta, Instant.now());
    }
}
