package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EmitirFacturaCommand(
        UUID clienteId,
        UUID pedidoId, // Opcional
        String rucCliente,
        List<LineaFacturaCommand> lineas
) {
    public record LineaFacturaCommand(
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            String moneda,
            List<ImpuestoCommand> impuestos
    ) {}

    public record ImpuestoCommand(
            String tipo,
            BigDecimal tarifa
    ) {}
}
