package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo;

/**
 * Enumeración: Estados del ciclo de vida de un Pedido de Venta.
 */
public enum EstadoPedido {

    /**
     * El pedido ha sido creado y se encuentra en edición de sus líneas de detalle.
     */
    PENDIENTE,

    /**
     * Alias compatible de inicio.
     */
    CREADO,

    /**
     * Borrador de pedido.
     */
    BORRADOR,

    /**
     * En proceso de reserva de stock con el Bounded Context de Inventario.
     */
    RESERVANDO_STOCK,

    /**
     * El pedido ha sido confirmado formalmente. Dispara eventos de dominio hacia inventario y facturación.
     */
    CONFIRMADO,

    /**
     * El pedido ha sido anulado o cancelado.
     */
    CANCELADO;

    /**
     * Indica si el pedido permite adición, modificación o eliminación de ítems/líneas.
     */
    public boolean esModificable() {
        return this == PENDIENTE || this == CREADO || this == BORRADOR;
    }
}
