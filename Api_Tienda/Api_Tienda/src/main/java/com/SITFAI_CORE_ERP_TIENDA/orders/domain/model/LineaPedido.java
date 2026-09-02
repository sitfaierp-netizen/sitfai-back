package com.SITFAI_CORE_ERP_TIENDA.orders.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ProductoId;

import java.util.Objects;
import java.util.UUID;

public class LineaPedido {
    
    private final UUID id;
    private final ProductoId productoId;
    private final int cantidad;
    private final Dinero precioUnitario;

    protected LineaPedido(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        if (productoId == null) {
            throw new IllegalArgumentException("El producto es obligatorio en la linea");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (precioUnitario == null) {
            throw new IllegalArgumentException("El precio unitario es obligatorio");
        }
        
        this.id = UUID.randomUUID();
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }
    
    // Reconstitution Constructor (for persistence layer)
    public LineaPedido(UUID id, ProductoId productoId, int cantidad, Dinero precioUnitario) {
        this.id = id;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
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
        return precioUnitario.multiplicar(cantidad);
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
}
