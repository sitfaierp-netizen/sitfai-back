package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.util.UUID;

/**
 * Value Object: Identificador único de Nota de Crédito.
 */
public record NotaCreditoId(UUID valor) {
    public NotaCreditoId {
        if (valor == null) {
            throw new IllegalArgumentException("El ID de la Nota de Crédito no puede ser nulo");
        }
    }
}
