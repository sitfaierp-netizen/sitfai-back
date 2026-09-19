package com.SITFAI_CORE_ERP_TIENDA.pos.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LineaDevolucionDto(
        UUID productoId,
        int cantidad,
        BigDecimal precioUnitario
) {}
