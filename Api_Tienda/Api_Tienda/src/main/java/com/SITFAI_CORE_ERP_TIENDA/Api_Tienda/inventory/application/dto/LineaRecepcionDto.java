package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record LineaRecepcionDto(
        UUID productoId,
        BigDecimal cantidad,
        LoteDto lote
) {
}
