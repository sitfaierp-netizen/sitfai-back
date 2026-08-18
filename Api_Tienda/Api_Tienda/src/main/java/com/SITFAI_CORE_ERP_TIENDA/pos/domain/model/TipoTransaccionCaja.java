package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model;

/**
 * Clasificación de los movimientos de dinero dentro de una sesión de Caja (CAJ-07).
 */
public enum TipoTransaccionCaja {
    /**
     * Cobro por venta efectuada al cliente (suma al saldo en caja).
     */
    VENTA,

    /**
     * Reembolso o anulación de venta al cliente (resta al saldo en caja / CAJ-08).
     */
    DEVOLUCION,

    /**
     * Entrada de efectivo externa (ej. inyección de cambio, fondo adicional).
     */
    INGRESO,

    /**
     * Salida de efectivo autorizada (ej. pago menor a proveedores, retiro de seguridad).
     */
    EGRESO
}
