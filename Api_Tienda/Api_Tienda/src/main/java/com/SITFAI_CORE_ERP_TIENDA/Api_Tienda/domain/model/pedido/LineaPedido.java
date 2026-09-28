package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de Dominio: Representa una lÃ­nea o Ã­tem de detalle dentro del Agregado {@link Pedido}.
 * <p>
 * Encapsula la cantidad solicitada, el producto y su precio unitario al momento de la venta.
 * Inmutable en su cÃ¡lculo de subtotal.
 * <p>
 * Regla REGLA-3: Entidad interna protegida por el Aggregate Root.
 */
public class LineaPedido {

    private final UUID id;
    private final ProductoId productoId;
    private final int cantidad;
    private final Dinero precioUnitario;

    public LineaPedido(UUID id, ProductoId productoId, int cantidad, Dinero precioUnitario) {
        this.id = id != null ? id : UUID.randomUUID();
        this.productoId = Objects.requireNonNull(productoId, "LineaPedido: productoId es obligatorio.");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("LineaPedido: la cantidad debe ser estrictamente positiva (> 0). Recibido: " + cantidad);
        }
        this.cantidad = cantidad;
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "LineaPedido: precioUnitario es obligatorio.");
    }

    public LineaPedido(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        this(UUID.randomUUID(), productoId, cantidad, precioUnitario);
    }

    public static LineaPedido crear(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        return new LineaPedido(productoId, cantidad, precioUnitario);
    }



    /**
     * Calcula el subtotal monetario de la lÃ­nea multiplicando la cantidad por el precio unitario.
     */
    public Dinero calcularSubtotal() {
        return precioUnitario.multiplicar(cantidad);
    }

    public Dinero subtotal() {
        return calcularSubtotal();
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public Dinero getPrecioUnitario() {
        return precioUnitario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaPedido that)) return false;
        return cantidad == that.cantidad &&
                Objects.equals(id, that.id) &&
                Objects.equals(productoId, that.productoId) &&
                Objects.equals(precioUnitario, that.precioUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productoId, cantidad, precioUnitario);
    }

    @Override
    public String toString() {
        return "LineaPedido{id=" + id + ", producto=" + productoId + ", cantidad=" + cantidad + ", precioUnitario=" + precioUnitario + "}";
    }
}

