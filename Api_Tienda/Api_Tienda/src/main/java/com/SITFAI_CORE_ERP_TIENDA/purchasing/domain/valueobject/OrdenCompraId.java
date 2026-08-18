package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.util.UUID;

public record OrdenCompraId(UUID valor) {
    public OrdenCompraId {
        if (valor == null) {
            throw new IllegalArgumentException("El ID de la Orden de Compra no puede ser nulo");
        }
    }
}
