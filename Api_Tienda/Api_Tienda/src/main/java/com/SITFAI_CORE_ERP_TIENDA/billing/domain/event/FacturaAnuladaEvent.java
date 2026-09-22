package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento de Dominio emitido cuando una Factura es anulada según AUD-04.
 * <p>
 * Reglas validadas: REGLA-3 (Domain Events inmutables), AUD-04 (Inmutabilidad Financiera), MT-01 (Tenant Isolation).
 */
public record FacturaAnuladaEvent(
        UUID eventoId,
        Instant ocurridoEn,
        UUID facturaId,
        UUID empresaId,
        String motivo
) implements DomainEvent {

    public FacturaAnuladaEvent {
        Objects.requireNonNull(eventoId, "eventoId es obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn es obligatorio");
        Objects.requireNonNull(facturaId, "facturaId es obligatorio");
        Objects.requireNonNull(empresaId, "empresaId es obligatorio (MT-01)");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de anulación es obligatorio (AUD-04).");
        }
    }

    public static FacturaAnuladaEvent of(FacturaId facturaId, EmpresaId empresaId, String motivo) {
        return new FacturaAnuladaEvent(
                UUID.randomUUID(),
                Instant.now(),
                facturaId.valor(),
                empresaId.valor(),
                motivo.trim()
        );
    }

    public static FacturaAnuladaEvent of(
            com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId facturaId,
            com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId empresaId,
            String motivo) {
        return new FacturaAnuladaEvent(
                UUID.randomUUID(),
                Instant.now(),
                facturaId.valor(),
                empresaId.valor(),
                motivo.trim()
        );
    }
}
