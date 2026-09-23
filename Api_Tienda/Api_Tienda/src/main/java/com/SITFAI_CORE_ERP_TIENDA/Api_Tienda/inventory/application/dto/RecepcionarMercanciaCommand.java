package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando inmutable (DTO de Aplicación) para registrar la recepción física de mercancía en Bodega.
 * <p>
 * Regla 1 (Clean Architecture): Cero dependencias técnicas de JPA, Spring o Jackson.
 * Regla BOD-04: Referencia inquebrantable a la Orden de Compra como documento fuente upstream.
 */
public record RecepcionarMercanciaCommand(
        UUID bodegaId,
        UUID ordenCompraId,
        List<LoteRecepcionCommand> lotes
) {

    public RecepcionarMercanciaCommand {
        Objects.requireNonNull(bodegaId, "RecepcionarMercanciaCommand: bodegaId no puede ser nulo.");
        Objects.requireNonNull(ordenCompraId, "RecepcionarMercanciaCommand: ordenCompraId no puede ser nulo (BOD-04).");
        lotes = lotes != null ? Collections.unmodifiableList(lotes) : Collections.emptyList();
    }

    public record LoteRecepcionCommand(
            UUID productoId,
            BigDecimal cantidad,
            String codigoLote,
            Instant fechaCaducidad
    ) {
        public LoteRecepcionCommand {
            Objects.requireNonNull(productoId, "LoteRecepcionCommand: productoId no puede ser nulo.");
            Objects.requireNonNull(cantidad, "LoteRecepcionCommand: cantidad no puede ser nula.");
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("LoteRecepcionCommand: la cantidad debe ser estrictamente mayor a cero.");
            }
        }
    }
}
