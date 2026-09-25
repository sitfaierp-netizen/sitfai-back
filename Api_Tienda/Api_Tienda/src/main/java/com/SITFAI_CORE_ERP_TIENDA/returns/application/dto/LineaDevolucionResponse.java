package com.SITFAI_CORE_ERP_TIENDA.returns.application.dto;

import java.util.UUID;

public record LineaDevolucionResponse(
        UUID id,
        UUID productoId,
        int cantidad,
        String motivo,
        String estadoInspeccion
) {}
