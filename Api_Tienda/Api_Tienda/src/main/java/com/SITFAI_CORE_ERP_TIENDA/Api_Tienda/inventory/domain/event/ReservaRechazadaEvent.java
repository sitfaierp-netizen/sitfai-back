package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de Integración emitido por el Bounded Context de Inventario
 * cuando una reserva de stock falla debido a la regla BOD-05 (Stock no negativo).
 * <p>
 * Reglas validadas: REGLA-3 (Domain Events inmutables), BOD-05 (Fallo fail-fast).
 */
public record ReservaRechazadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        UUID pedidoId,
        ProductoId productoId,
        String motivo
) implements DomainEvent {

    public static ReservaRechazadaEvent of(UUID pedidoId, ProductoId productoId, String motivo) {
        return new ReservaRechazadaEvent(
                UUID.randomUUID(),
                Instant.now(),
                pedidoId,
                productoId,
                motivo
        );
    }
}
