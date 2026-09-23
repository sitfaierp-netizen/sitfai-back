package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO inmutable para la apertura de un Turno de Caja.
 * <p>
 * Regla MT-02: El empresa_id no se solicita en el request web, sino que se extrae
 * de forma segura delegando en Keycloak vía el puerto TenantProviderPort.
 */
public record AbrirTurnoCommand(
        UUID empresaId,
        UUID cajaId,
        UUID sucursalId,
        UUID cajeroId,
        BigDecimal montoApertura
) {

    public AbrirTurnoCommand(UUID cajaId, UUID cajeroId, BigDecimal montoApertura) {
        this(null, cajaId, null, cajeroId, montoApertura);
    }

    public AbrirTurnoCommand {
        Objects.requireNonNull(cajaId, "El cajaId es obligatorio");
        Objects.requireNonNull(cajeroId, "El cajeroId es obligatorio");
        Objects.requireNonNull(montoApertura, "El monto de apertura es obligatorio");
    }

    public UUID usuarioId() {
        return cajeroId;
    }
}
