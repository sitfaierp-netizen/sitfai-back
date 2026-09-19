package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo;

import java.util.UUID;

/**
 * Value Object: Referencia a la Orden de Compra origen (Cumple regla BOD-04: Documento Fuente).
 */
public record OrdenCompraId(UUID valor) {
    public OrdenCompraId {
        if (valor == null) {
            throw new IllegalArgumentException("OrdenCompraId: el UUID no puede ser null.");
        }
    }
}
