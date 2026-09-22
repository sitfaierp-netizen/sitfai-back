package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Integración emitido por el Bounded Context de Inventario
 * cuando una reserva de stock falla debido a la regla BOD-05 (Stock no negativo).
 * <p>
 * Reglas validadas: REGLA-3 (Domain Events inmutables), BOD-05 (Fallo fail-fast), MT-01 (Aislamiento Multitenant).
 */
public record ReservaRechazadaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        EmpresaId empresaId,
        UUID pedidoId,
        ProductoId productoId,
        String motivo
) implements DomainEvent {

    public ReservaRechazadaEvent {
        Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
        Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01)");
        Objects.requireNonNull(pedidoId, "pedidoId es obligatorio");
        Objects.requireNonNull(productoId, "productoId es obligatorio");
        if (motivo == null || motivo.isBlank()) {
            motivo = "Rechazado por regla BOD-05: Stock insuficiente";
        }
    }

    public static ReservaRechazadaEvent of(EmpresaId empresaId, UUID pedidoId, ProductoId productoId, String motivo) {
        return new ReservaRechazadaEvent(
                UUID.randomUUID(),
                Instant.now(),
                empresaId,
                pedidoId,
                productoId,
                motivo
        );
    }

    public static ReservaRechazadaEvent of(UUID empresaId, UUID pedidoId, ProductoId productoId, String motivo) {
        return of(new EmpresaId(empresaId), pedidoId, productoId, motivo);
    }
}
