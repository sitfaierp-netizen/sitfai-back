package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo;

import java.time.LocalDate;

/**
 * Value Object: Lote de inventario.
 * Contiene la información vital para el algoritmo FEFO.
 */
public record Lote(String codigoLote, LocalDate fechaCaducidad) {
    public Lote {
        if (codigoLote == null || codigoLote.isBlank()) {
            throw new IllegalArgumentException("Lote: el codigoLote no puede ser nulo o vacío.");
        }
        if (fechaCaducidad == null) {
            throw new IllegalArgumentException("Lote: la fechaCaducidad es obligatoria para el cálculo FEFO.");
        }
    }
}
