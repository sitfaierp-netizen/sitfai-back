package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model;

/**
 * Enumeración de tipos de movimiento de inventario en el dominio de Bodega.
 * <p>
 * Un movimiento de ENTRADA incrementa el stock (ej. recepción de Orden de Compra).
 * Un movimiento de SALIDA decrementa el stock (ej. despacho por Venta, transferencia saliente).
 * <p>
 * BOD-06: Las transferencias entre Bodegas generan dos movimientos: SALIDA en la
 * Bodega origen y ENTRADA en la Bodega destino.
 * <p>
 * Reglas validadas: BOD-03, BOD-06, MCP-01.
 */
public enum TipoMovimiento {

    /**
     * Incrementa el stock en la Bodega.
     * Ejemplos: Recepción de Orden de Compra, Devolución de cliente, Transferencia entrante.
     */
    ENTRADA,

    /**
     * Decrementa el stock en la Bodega. Sujeto a la invariante BOD-05.
     * Ejemplos: Despacho por Venta, Transferencia saliente, Ajuste de merma.
     */
    SALIDA
}
