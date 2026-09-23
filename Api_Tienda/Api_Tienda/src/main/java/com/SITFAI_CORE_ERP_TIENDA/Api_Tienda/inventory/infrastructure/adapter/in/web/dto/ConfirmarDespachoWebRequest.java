package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConfirmarDespachoCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO (Infraestructura Web) para confirmar el despacho de mercancía.
 * <p>
 * Regla 1 (Aislamiento): Desacoplado del Command de Aplicación.
 * Regla 5 (API REST): Validaciones Jakarta Bean Validation.
 * Regla MT-02: Cero empresa_id en el payload (se resuelve vía JWT / Security Context).
 */
public record ConfirmarDespachoWebRequest(
        @NotNull(message = "El pedidoId es obligatorio (BOD-04)")
        UUID pedidoId,

        UUID bodegaId,

        @NotEmpty(message = "Debe incluir al menos una línea a despachar")
        @Valid
        List<LineaDespachoWebRequest> lineas
) {

    public ConfirmarDespachoCommand toCommand() {
        List<ConfirmarDespachoCommand.LineaDespachoCommand> lineasCmd = lineas != null
                ? lineas.stream()
                .map(l -> new ConfirmarDespachoCommand.LineaDespachoCommand(l.productoId(), l.cantidad()))
                .toList()
                : Collections.emptyList();

        return new ConfirmarDespachoCommand(pedidoId, bodegaId, lineasCmd);
    }

    public record LineaDespachoWebRequest(
            @NotNull(message = "El productoId es obligatorio")
            UUID productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            BigDecimal cantidad
    ) {
    }
}
