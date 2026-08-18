package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO: Comando inmutable para emitir una Nota de Crédito.
 */
public record EmitirNotaCreditoCommand(
        UUID empresaId,
        UUID facturaAfectadaId,
        String codigoMotivo,
        String descripcionMotivo,
        List<LineaReversoDto> lineas
) {
    public record LineaReversoDto(
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            String moneda,
            List<ImpuestoReversoDto> impuestos
    ) {}

    public record ImpuestoReversoDto(
            String tipo,
            BigDecimal tarifa
    ) {}
}
