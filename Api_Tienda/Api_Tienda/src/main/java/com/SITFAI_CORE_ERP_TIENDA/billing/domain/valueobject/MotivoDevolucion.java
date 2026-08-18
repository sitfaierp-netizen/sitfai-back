package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

/**
 * Value Object: Motivo por el cual se emite la Nota de Crédito.
 */
public record MotivoDevolucion(String codigo, String descripcion) {
    public MotivoDevolucion {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del motivo no puede ser vacío");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción del motivo no puede ser vacía");
        }
    }
}
