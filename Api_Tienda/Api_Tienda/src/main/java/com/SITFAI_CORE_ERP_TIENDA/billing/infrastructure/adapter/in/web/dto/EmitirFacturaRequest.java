package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EmitirFacturaRequest(
        @NotNull(message = "El clienteId es obligatorio")
        UUID clienteId,

        UUID pedidoId,

        @NotBlank(message = "El RUC del cliente es obligatorio")
        String rucCliente,

        @NotEmpty(message = "La factura debe tener al menos una línea")
        @Valid
        List<LineaFacturaRequest> lineas
) {
    public record LineaFacturaRequest(
            @NotBlank(message = "El concepto es obligatorio")
            String concepto,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            BigDecimal cantidad,

            @NotNull(message = "El precio unitario es obligatorio")
            @Positive(message = "El precio unitario debe ser mayor a cero")
            BigDecimal precioUnitario,

            @NotBlank(message = "La moneda es obligatoria")
            String moneda,

            @Valid
            List<ImpuestoRequest> impuestos
    ) {}

    public record ImpuestoRequest(
            @NotBlank(message = "El tipo de impuesto es obligatorio")
            String tipo,

            @NotNull(message = "La tarifa es obligatoria")
            @Positive(message = "La tarifa debe ser positiva")
            BigDecimal tarifa
    ) {}
}
