package com.SITFAI_CORE_ERP_TIENDA.returns.application.dto;

import java.util.UUID;

public record LineaDevolucionCommand(
        UUID productoId,
        int cantidad,
        String motivo
) {}
