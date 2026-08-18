package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio: Línea de detalle de un {@link Pedido}.
 * <p>
 * Representa un ítem solicitado, su cantidad y su precio unitario.
 * Es administrada exclusivamente por la Raíz del Agregado {@code Pedido} (REGLA 3).
 */
public class LineaPedido {

    private final UUID id;
    private final ProductoId productoId;
    private Cantidad cantidad;
    private final Dinero precioUnitario;

    public LineaPedido(UUID id, ProductoId productoId, Cantidad cantidad, Dinero precioUnitario) {
        this.id = Objects.requireNonNull(id, "LineaPedido: id es obligatorio.");
        this.productoId = Objects.requireNonNull(productoId, "LineaPedido: productoId es obligatorio.");
        this.cantidad = Objects.requireNonNull(cantidad, "LineaPedido: cantidad es obligatoria.");
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "LineaPedido: precioUnitario es obligatorio.");
    }

    public LineaPedido(UUID id, ProductoId productoId, int cantidad, Dinero precioUnitario) {
        this(id, productoId, Cantidad.de(cantidad), precioUnitario);
    }

    public static LineaPedido crear(ProductoId productoId, Cantidad cantidad, Dinero precioUnitario) {
        return new LineaPedido(UUID.randomUUID(), productoId, cantidad, precioUnitario);
    }

    public static LineaPedido crear(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        return new LineaPedido(UUID.randomUUID(), productoId, Cantidad.de(cantidad), precioUnitario);
    }

    public Dinero subtotal() {
        return this.precioUnitario.multiplicar(this.cantidad.valor());
    }

    public void aumentarCantidad(Cantidad adicional) {
        Objects.requireNonNull(adicional, "LineaPedido: adicional no puede ser null.");
        this.cantidad = this.cantidad.sumar(adicional);
    }

    public void aumentarCantidad(int unidades) {
        this.cantidad = this.cantidad.sumar(unidades);
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public int getCantidad() {
        return cantidad.valor();
    }

    public Cantidad getCantidadVO() {
        return cantidad;
    }

    public Dinero getPrecioUnitario() {
        return precioUnitario;
    }

    public ItemPedido aItemPedido() {
        return new ItemPedido(id, productoId, cantidad, precioUnitario);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineaPedido that = (LineaPedido) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LineaPedido{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal() +
                '}';
    }
}
