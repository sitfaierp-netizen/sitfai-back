package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna protegida del Agregado Despacho (Outbound Logistics).
 * <p>
 * Regla 3 (DDD): Entidad encapsulada, solo accesible y mutable a través del Aggregate Root Despacho.
 */
public class LineaDespacho {

    private final UUID id;
    private final ProductoId productoId;
    private final Cantidad cantidad;

    public LineaDespacho(UUID id, ProductoId productoId, Cantidad cantidad) {
        this.id = Objects.requireNonNull(id, "LineaDespacho: id es obligatorio.");
        this.productoId = Objects.requireNonNull(productoId, "LineaDespacho: productoId es obligatorio.");
        this.cantidad = Objects.requireNonNull(cantidad, "LineaDespacho: cantidad es obligatoria.");
    }

    public LineaDespacho(ProductoId productoId, Cantidad cantidad) {
        this(UUID.randomUUID(), productoId, cantidad);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineaDespacho that = (LineaDespacho) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
