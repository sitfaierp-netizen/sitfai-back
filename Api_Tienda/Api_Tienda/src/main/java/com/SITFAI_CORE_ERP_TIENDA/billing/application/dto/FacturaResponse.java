package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO: Respuesta plana de una Factura Electrónica.
 */
public record FacturaResponse(
        UUID facturaId,
        UUID empresaId,
        String nitEmisor,
        String nitReceptor,
        String estado,
        String cufe,
        BigDecimal subtotal,
        BigDecimal totalImpuestos,
        BigDecimal totalGeneral,
        List<LineaFacturaResponse> lineas
) {
    public record LineaFacturaResponse(
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal,
            BigDecimal totalImpuestos
    ) {}
}
