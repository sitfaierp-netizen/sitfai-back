package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO inmutable para el cierre de un Turno de Caja y Arqueo Financiero.
 */
public record CerrarTurnoCommand(
        UUID empresaId,
        UUID turnoId,
        BigDecimal montoFisicoDeclarado
) {

    public CerrarTurnoCommand(UUID turnoId, BigDecimal montoFisicoDeclarado) {
        this(null, turnoId, montoFisicoDeclarado);
    }

    public CerrarTurnoCommand {
        Objects.requireNonNull(turnoId, "El turnoId es obligatorio");
        Objects.requireNonNull(montoFisicoDeclarado, "El monto físico declarado es obligatorio");
    }

    public BigDecimal montoDeclarado() {
        return montoFisicoDeclarado;
    }
}
