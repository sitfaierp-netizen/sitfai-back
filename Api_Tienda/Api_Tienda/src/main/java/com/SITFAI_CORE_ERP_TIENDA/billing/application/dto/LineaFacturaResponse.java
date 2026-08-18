package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO interno para reflejar una línea de factura y sus cálculos matemáticos.
 */
public record LineaFacturaResponse(
        UUID id,
        String productoId,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        String moneda,
        BigDecimal porcentajeImpuesto,
        BigDecimal subtotalSinImpuesto,
        BigDecimal montoImpuesto,
        BigDecimal totalLinea
) {}
