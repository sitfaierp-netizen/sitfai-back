package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Web DTO: Petición HTTP para emitir una Factura Electrónica.
 */
public record FacturaWebRequest(
        String nitEmisor,
        String nitReceptor,
        List<LineaFacturaWebRequest> lineas
) {
    public record LineaFacturaWebRequest(
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            String moneda,
            List<ImpuestoWebRequest> impuestos
    ) {}

    public record ImpuestoWebRequest(
            String tipo,
            BigDecimal tarifa
    ) {}
}
