package com.SITFAI_CORE_ERP_TIENDA.billing.application.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Comando inmutable (DTO puro de aplicación) para solicitar la emisión de una Factura.
 * <p>
 * Regla 1 (Clean Architecture): Cero anotaciones de framework (ni Spring, ni JPA, ni Jackson).
 * Regla MT-01: El {@code empresaId} es obligatorio en cualquier flujo.
 */
public record EmitirFacturaCommand(
        UUID empresaId,
        UUID clienteId,
        UUID pedidoId,
        String rucCliente,
        List<LineaFacturaCommand> lineas,
        String tipoOrigen,
        UUID documentoFuenteId
) {

    public EmitirFacturaCommand {
        lineas = lineas != null ? Collections.unmodifiableList(lineas) : Collections.emptyList();
    }

    public EmitirFacturaCommand(
            UUID empresaId,
            UUID clienteId,
            UUID pedidoId,
            String rucCliente,
            List<LineaFacturaCommand> lineas) {
        this(
                empresaId,
                clienteId,
                pedidoId,
                rucCliente,
                lineas,
                pedidoId != null ? "ECOMMERCE" : "DIRECTA",
                pedidoId
        );
    }

    public EmitirFacturaCommand(
            UUID empresaId,
            UUID clienteId,
            String tipoOrigen,
            UUID documentoFuenteId,
            List<LineaFacturaCommand> lineas) {
        this(
                empresaId,
                clienteId,
                null,
                null,
                lineas,
                tipoOrigen,
                documentoFuenteId
        );
    }

    public record LineaFacturaCommand(
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            String moneda,
            List<ImpuestoCommand> impuestos
    ) {
        public LineaFacturaCommand {
            impuestos = impuestos != null ? Collections.unmodifiableList(impuestos) : Collections.emptyList();
        }

        public LineaFacturaCommand(String concepto, BigDecimal cantidad, BigDecimal precioUnitario) {
            this(concepto, cantidad, precioUnitario, "COP", Collections.emptyList());
        }

        public LineaFacturaCommand(String concepto, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal porcentajeImpuesto) {
            this(
                    concepto,
                    cantidad,
                    precioUnitario,
                    "COP",
                    porcentajeImpuesto != null && porcentajeImpuesto.compareTo(BigDecimal.ZERO) > 0
                            ? List.of(new ImpuestoCommand("IVA", porcentajeImpuesto))
                            : Collections.emptyList()
            );
        }
    }

    public record ImpuestoCommand(
            String tipo,
            BigDecimal tarifa
    ) {}
}
