package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record FacturaResponse(
        String id,
        String empresaId,
        String clienteId,
        String pedidoId,
        String rucCliente,
        BigDecimal subtotal,
        BigDecimal totalImpuestos,
        BigDecimal totalGeneral,
        String estado,
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
