package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO Web: Petición para agregar o actualizar una línea de detalle.
 */
public record AgregarLineaWebRequest(
        UUID productoId,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        String moneda
) {
}
