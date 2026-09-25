package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo;

/**
 * Value Object: Cantidad física auditada durante el conteo cíclico.
 * <p>
 * Regla: Entero mayor o igual a cero (>= 0) con validación fail-fast.
 * Regla REGLA-3: Record Java 21 inmutable.
 * Regla REGLA-1: Cero dependencias de frameworks externos.
 */
public record CantidadFisica(int valor) {

    public CantidadFisica {
        if (valor < 0) {
            throw new IllegalArgumentException("CantidadFisica: el valor no puede ser negativo. Recibido: " + valor);
        }
    }

    public static CantidadFisica de(int valor) {
        return new CantidadFisica(valor);
    }

    public static CantidadFisica cero() {
        return new CantidadFisica(0);
    }

    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}
