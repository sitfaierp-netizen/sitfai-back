package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.ProductoId;

import java.util.Objects;
import java.util.UUID;

public class LineaDespacho {
    private final UUID id;
    private final ProductoId productoId;
    private final int cantidad;

    public LineaDespacho(ProductoId productoId, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.id = UUID.randomUUID();
        this.productoId = Objects.requireNonNull(productoId, "El productoId no puede ser nulo");
        this.cantidad = cantidad;
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
}
