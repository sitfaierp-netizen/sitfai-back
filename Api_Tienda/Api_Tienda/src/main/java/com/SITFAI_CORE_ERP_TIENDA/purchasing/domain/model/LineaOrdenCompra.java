package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad local dentro del Agregado OrdenCompra.
 */
public class LineaOrdenCompra {
    
    private final ProductoId productoId;
    private final BigDecimal cantidadSolicitada;
    private final Dinero costoUnitarioPactado;

    public LineaOrdenCompra(ProductoId productoId, BigDecimal cantidadSolicitada, Dinero costoUnitarioPactado) {
        this.productoId = Objects.requireNonNull(productoId, "El ProductoId es obligatorio");
        this.cantidadSolicitada = Objects.requireNonNull(cantidadSolicitada, "La cantidad solicitada es obligatoria");
        this.costoUnitarioPactado = Objects.requireNonNull(costoUnitarioPactado, "El costo unitario es obligatorio");
        
        if (this.cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a cero");
        }
    }

    public Dinero calcularSubtotal() {
        return costoUnitarioPactado.multiplicar(cantidadSolicitada);
    }

    public ProductoId getProductoId() { return productoId; }
    public BigDecimal getCantidadSolicitada() { return cantidadSolicitada; }
    public Dinero getCostoUnitarioPactado() { return costoUnitarioPactado; }
}
