package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad Local de Dominio: {@code ItemPedido}.
 * <p>
 * Registra el producto, la cantidad y el precio unitario pactado para una línea del pedido.
 * Administrada exclusivamente por la Raíz del Agregado {@link Pedido} (REGLA 3).
 * <p>
 * Aislamiento total: Java 25 puro, sin frameworks web ni JPA (REGLA 1, MCP-01).
 */
public class ItemPedido {

    private final UUID id;
    private final ProductoId productoId;
    private Cantidad cantidad;
    private final Dinero precioUnitario;

    public ItemPedido(UUID id, ProductoId productoId, Cantidad cantidad, Dinero precioUnitario) {
        this.id = Objects.requireNonNull(id, "ItemPedido: id es obligatorio.");
        this.productoId = Objects.requireNonNull(productoId, "ItemPedido: productoId es obligatorio.");
        this.cantidad = Objects.requireNonNull(cantidad, "ItemPedido: cantidad es obligatoria.");
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "ItemPedido: precioUnitario es obligatorio.");
    }

    public static ItemPedido crear(ProductoId productoId, Cantidad cantidad, Dinero precioUnitario) {
        return new ItemPedido(UUID.randomUUID(), productoId, cantidad, precioUnitario);
    }

    public static ItemPedido crear(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        return new ItemPedido(UUID.randomUUID(), productoId, Cantidad.de(cantidad), precioUnitario);
    }

    /**
     * Calcula el subtotal de este ítem (precioUnitario * cantidad).
     */
    public Dinero subtotal() {
        return this.precioUnitario.multiplicar(this.cantidad.valor());
    }

    /**
     * Incrementa la cantidad de este ítem.
     */
    public void aumentarCantidad(Cantidad adicional) {
        Objects.requireNonNull(adicional, "ItemPedido: adicional no puede ser null.");
        this.cantidad = this.cantidad.sumar(adicional);
    }

    /**
     * Incrementa la cantidad de este ítem en unidades enteras.
     */
    public void aumentarCantidad(int unidades) {
        this.cantidad = this.cantidad.sumar(unidades);
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public Cantidad getCantidad() {
        return cantidad;
    }

    public Dinero getPrecioUnitario() {
        return precioUnitario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedido that = (ItemPedido) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal() +
                '}';
    }
}
