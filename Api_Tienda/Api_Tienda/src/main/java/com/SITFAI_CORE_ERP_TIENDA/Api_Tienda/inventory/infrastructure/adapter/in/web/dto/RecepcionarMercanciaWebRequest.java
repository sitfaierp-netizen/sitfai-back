package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RecepcionarMercanciaCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO (Infraestructura Web) para la recepción física de mercancía en bodega.
 * <p>
 * Regla 1 (Aislamiento): Desacoplado del Command de Aplicación.
 * Regla 5 (API REST): Objeto dedicado a la capa HTTP con validaciones Jakarta Bean Validation.
 * Regla MT-02: Cero empresa_id o tenant en el payload; el tenant se resuelve internamente.
 */
public record RecepcionarMercanciaWebRequest(
        @NotNull(message = "El id de la orden de compra es obligatorio (BOD-04)")
        UUID ordenCompraId,

        @NotEmpty(message = "Debe registrar al menos un lote a ingresar")
        @Valid
        List<LoteIngresoWebRequest> lotes
) {

    public RecepcionarMercanciaCommand toCommand(UUID bodegaId) {
        List<RecepcionarMercanciaCommand.LoteRecepcionCommand> lotesCmd = lotes != null
                ? lotes.stream()
                .map(l -> new RecepcionarMercanciaCommand.LoteRecepcionCommand(
                        l.productoId(),
                        l.cantidad(),
                        l.codigoLote(),
                        l.fechaCaducidad()
                ))
                .toList()
                : Collections.emptyList();

        return new RecepcionarMercanciaCommand(bodegaId, ordenCompraId, lotesCmd);
    }

    public record LoteIngresoWebRequest(
            @NotNull(message = "El productoId es obligatorio")
            UUID productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            BigDecimal cantidad,

            String codigoLote,

            Instant fechaCaducidad
    ) {
    }
}
