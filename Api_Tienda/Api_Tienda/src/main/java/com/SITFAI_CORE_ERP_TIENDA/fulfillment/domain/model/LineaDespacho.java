package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.ProductoId;

import java.util.Objects;
import java.util.UUID;

public class LineaDespacho {
    
    private final UUID id;
    private final ProductoId productoId;
    private final Cantidad cantidadSolicitada;
    private Cantidad cantidadPreparada;

    private LineaDespacho(UUID id, ProductoId productoId, Cantidad cantidadSolicitada) {
        this.id = Objects.requireNonNull(id, "El ID de la línea no puede ser nulo");
        this.productoId = Objects.requireNonNull(productoId, "El productoId no puede ser nulo");
        this.cantidadSolicitada = Objects.requireNonNull(cantidadSolicitada, "La cantidad solicitada no puede ser nula");
        this.cantidadPreparada = Cantidad.cero();
    }

    public static LineaDespacho crear(ProductoId productoId, Cantidad cantidadSolicitada) {
        return new LineaDespacho(UUID.randomUUID(), productoId, cantidadSolicitada);
    }

    public void incrementarPreparada(Cantidad cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad a preparar no puede ser nula");
        Cantidad nuevaCantidad = this.cantidadPreparada.sumar(cantidad);
        
        if (nuevaCantidad.esMayorQue(this.cantidadSolicitada)) {
            throw new IllegalStateException("No se puede preparar más cantidad de la solicitada para el producto " + productoId.value());
        }
        
        this.cantidadPreparada = nuevaCantidad;
    }

    public boolean estaCompletada() {
        return this.cantidadPreparada.value() == this.cantidadSolicitada.value();
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public Cantidad getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public Cantidad getCantidadPreparada() {
        return cantidadPreparada;
    }
}
