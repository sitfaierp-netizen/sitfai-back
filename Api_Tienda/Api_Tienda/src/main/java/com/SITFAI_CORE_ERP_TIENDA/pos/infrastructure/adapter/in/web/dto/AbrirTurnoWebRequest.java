package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request Web DTO para la apertura de un Turno de Caja.
 * <p>
 * Regla MT-02: El empresaId no se recibe en este request, se extrae del token JWT.
 */
public record AbrirTurnoWebRequest(
        UUID cajaId,
        UUID sucursalId,
        UUID usuarioId,
        UUID cajeroId,
        BigDecimal montoApertura
) {

    public AbrirTurnoWebRequest(UUID cajaId, UUID sucursalId, UUID usuarioId, BigDecimal montoApertura) {
        this(cajaId, sucursalId, usuarioId, usuarioId, montoApertura);
    }

    public UUID getCajeroId() {
        return cajeroId != null ? cajeroId : usuarioId;
    }
}
