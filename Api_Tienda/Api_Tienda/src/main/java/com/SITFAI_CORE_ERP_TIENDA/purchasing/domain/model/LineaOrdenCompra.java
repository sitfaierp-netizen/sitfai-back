package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProductoId;

import java.util.Objects;
import java.util.UUID;

public class LineaOrdenCompra {

    private final UUID id;
    private final ProductoId productoId;
    private final int cantidad;
    private final Dinero precioUnitario;
    private final Dinero subtotal;

    public LineaOrdenCompra(UUID id, ProductoId productoId, int cantidad, Dinero precioUnitario) {
        this.id = Objects.requireNonNull(id, "El ID de la línea no puede ser nulo.");
        this.productoId = Objects.requireNonNull(productoId, "El productoId no puede ser nulo.");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        this.cantidad = cantidad;
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "El precio unitario no puede ser nulo.");
        this.subtotal = this.precioUnitario.multiplicar(this.cantidad);
    }

    static LineaOrdenCompra crear(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        return new LineaOrdenCompra(UUID.randomUUID(), productoId, cantidad, precioUnitario);
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

    public Dinero getSubtotal() {
        return subtotal;
    }
}
