package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Request Web DTO para el cierre de un Turno de Caja y declaración física de efectivo.
 */
public record CerrarTurnoWebRequest(
        BigDecimal montoDeclarado,
        BigDecimal montoFisicoDeclarado
) {

    public CerrarTurnoWebRequest(BigDecimal montoDeclarado) {
        this(montoDeclarado, montoDeclarado);
    }

    public BigDecimal getMontoFisico() {
        return montoFisicoDeclarado != null ? montoFisicoDeclarado : montoDeclarado;
    }
}
