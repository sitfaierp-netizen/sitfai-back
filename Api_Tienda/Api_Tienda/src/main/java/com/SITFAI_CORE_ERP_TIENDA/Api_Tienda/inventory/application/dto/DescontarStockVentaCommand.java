package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

public record DescontarStockVentaCommand(
        String bodegaId,
        String productoId,
        BigDecimal cantidad,
        String documentoFuenteId) {
    public DescontarStockVentaCommand {
        Objects.requireNonNull(bodegaId, "BodegaId es obligatorio");
        Objects.requireNonNull(productoId, "ProductoId es obligatorio");
        Objects.requireNonNull(cantidad, "Cantidad es obligatoria");
        Objects.requireNonNull(documentoFuenteId, "DocumentoFuenteId es obligatorio");
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor a cero");
        }
    }
}
