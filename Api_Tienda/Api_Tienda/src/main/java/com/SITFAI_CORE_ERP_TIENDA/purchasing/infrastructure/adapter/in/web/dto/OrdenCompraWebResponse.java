package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO Web: Representación de salida para una orden de compra.
 */
public record OrdenCompraWebResponse(
        UUID id,
        UUID empresaId,
        UUID proveedorId,
        String estado,
        BigDecimal total,
        String moneda,
        List<LineaOrdenWebResponse> lineas,
        Instant creadoEn,
        Instant actualizadoEn
) {
}
