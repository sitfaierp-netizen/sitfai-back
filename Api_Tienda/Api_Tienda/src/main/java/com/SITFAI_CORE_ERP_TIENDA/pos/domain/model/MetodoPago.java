package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model;

/**
 * Enum: Métodos de pago aceptados en el punto de venta.
 * Regla CAJ-01: Toda venta debe tener un método de pago definido.
 */
public enum MetodoPago {
    EFECTIVO,
    TARJETA_CREDITO,
    TARJETA_DEBITO,
    TRANSFERENCIA,
    MIXTO
}
