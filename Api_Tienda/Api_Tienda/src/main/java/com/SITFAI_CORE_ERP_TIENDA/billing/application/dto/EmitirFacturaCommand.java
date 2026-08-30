package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Comando para emitir una factura.
 * El empresaId se incluye como campo para soportar flujos de evento (PedidoConfirmado),
 * donde el tenant se extrae del evento mismo. En flujos REST, el controller lo inyecta
 * desde @TenantId (MT-02 Zero Trust).
 */
public record EmitirFacturaCommand(
        UUID empresaId,        // MT-01: obligatorio. En REST viene del JWT, en eventos del event payload
        UUID clienteId,
        UUID pedidoId,         // Opcional
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
