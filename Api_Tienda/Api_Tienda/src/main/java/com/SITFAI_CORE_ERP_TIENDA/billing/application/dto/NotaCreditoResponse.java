package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO: Respuesta plana de una Nota de Crédito.
 */
public record NotaCreditoResponse(
        UUID notaCreditoId,
        UUID empresaId,
        UUID facturaAfectadaId,
        String estado,
        String cufe,
        BigDecimal subtotal,
        BigDecimal totalImpuestos,
        BigDecimal totalGeneral,
        List<LineaReversoResponse> lineasReversadas
) {
    public record LineaReversoResponse(
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal,
            BigDecimal totalImpuestos
    ) {}
}
