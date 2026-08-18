package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Web Request DTO: Parámetros HTTP para una línea de factura.
 */
public record LineaFacturaWebRequest(
        String productoId,
        String descripcion,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        String moneda,
        BigDecimal porcentajeImpuesto
) {}
