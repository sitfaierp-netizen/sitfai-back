package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model;

/**
 * Estado del ciclo de vida de un Pedido en el Bounded Context de Api_Tienda (Puerto 8084).
 */
public enum EstadoPedido {
    CREADO,
    BORRADOR,
    RESERVANDO_STOCK,
    CONFIRMADO,
    CANCELADO;

    /**
     * Indica si el pedido permite adición, modificación o eliminación de ítems.
     */
    public boolean esModificable() {
        return this == CREADO || this == BORRADOR;
    }
}
