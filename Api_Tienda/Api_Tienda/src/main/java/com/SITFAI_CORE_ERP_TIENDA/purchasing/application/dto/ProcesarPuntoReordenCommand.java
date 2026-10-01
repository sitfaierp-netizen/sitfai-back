package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Trusted internal command created exclusively from an Inventory domain event. */
public record ProcesarPuntoReordenCommand(
        UUID empresaId,
        UUID bodegaId,
        UUID productoId,
        BigDecimal cantidadActual
) {
    public ProcesarPuntoReordenCommand {
        Objects.requireNonNull(empresaId, "empresaId es obligatorio");
        Objects.requireNonNull(bodegaId, "bodegaId es obligatorio");
        Objects.requireNonNull(productoId, "productoId es obligatorio");
        Objects.requireNonNull(cantidadActual, "cantidadActual es obligatoria");
    }
}
