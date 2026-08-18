package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;

/**
 * DTO interno para las líneas de detalle de una factura.
 */
public record LineaFacturaCommand(
        String productoId,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        String moneda,
        BigDecimal porcentajeImpuesto
) {}
