package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.util.UUID;

public record CrearCajaCommand(
        UUID empresaId,
        UUID sucursalId,
        String nombreCaja
) {
}
