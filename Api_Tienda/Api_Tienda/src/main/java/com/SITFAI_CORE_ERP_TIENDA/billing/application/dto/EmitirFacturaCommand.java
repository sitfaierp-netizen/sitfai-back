package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO: Comando inmutable para emitir una factura.
 * Cero frameworks de validación.
 */
public record EmitirFacturaCommand(
        UUID empresaId,
        String nitEmisor,
        String nitReceptor,
        List<LineaFacturaDto> lineas
) {
    public record LineaFacturaDto(
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            String moneda,
            List<ImpuestoDto> impuestos
    ) {}

    public record ImpuestoDto(
            String tipo,
            BigDecimal tarifa
    ) {}
}
