package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.CantidadRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.ProductoId;

import java.util.Objects;
import java.util.UUID;

public class LineaRecepcion {
    private final UUID id;
    private final ProductoId productoId;
    private final CantidadRecepcion cantidadEsperada;
    private CantidadRecepcion cantidadRecibida;

    public LineaRecepcion(UUID id, ProductoId productoId, CantidadRecepcion cantidadEsperada, CantidadRecepcion cantidadRecibida) {
        this.id = id != null ? id : UUID.randomUUID();
        this.productoId = Objects.requireNonNull(productoId, "ProductoId no puede ser nulo.");
        this.cantidadEsperada = Objects.requireNonNull(cantidadEsperada, "Cantidad esperada no puede ser nula.");
        this.cantidadRecibida = cantidadRecibida != null ? cantidadRecibida : new CantidadRecepcion(0);
    }

    public LineaRecepcion(ProductoId productoId, CantidadRecepcion cantidadEsperada) {
        this(UUID.randomUUID(), productoId, cantidadEsperada, new CantidadRecepcion(0));
    }

    public UUID getId() {
        return id;
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public CantidadRecepcion getCantidadEsperada() {
        return cantidadEsperada;
    }

    public CantidadRecepcion getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void recibir(CantidadRecepcion cantidadAdicional) {
        if (cantidadAdicional == null) {
            throw new IllegalArgumentException("La cantidad a recibir no puede ser nula.");
        }
        CantidadRecepcion nuevaCantidad = this.cantidadRecibida.sumar(cantidadAdicional);
        if (nuevaCantidad.valor() > this.cantidadEsperada.valor()) {
            throw new IllegalStateException("Sobre-entrega rechazada: La cantidad recibida excede la cantidad esperada.");
        }
        this.cantidadRecibida = nuevaCantidad;
    }

    public boolean estaCompleta() {
        return this.cantidadRecibida.valor() == this.cantidadEsperada.valor();
    }
}
