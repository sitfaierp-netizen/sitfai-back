package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProductoId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad: Línea de Solicitud de Abastecimiento.
 * Pertenece exclusivamente al Agregado SolicitudAbastecimiento.
 * Cero dependencias a Spring, JPA o Lombok (Regla 1).
 */
public class LineaSolicitud {

    private final ProductoId productoId;
    private final BigDecimal cantidadSolicitada;

    private LineaSolicitud(ProductoId productoId, BigDecimal cantidadSolicitada) {
        this.productoId = Objects.requireNonNull(productoId, "El ProductoId de la línea no puede ser nulo.");
        if (cantidadSolicitada == null || cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "La cantidadSolicitada debe ser mayor a cero. Recibido: " + cantidadSolicitada);
        }
        this.cantidadSolicitada = cantidadSolicitada;
    }

    /**
     * Factory method — único punto de creación de una LineaSolicitud válida.
     */
    public static LineaSolicitud crear(ProductoId productoId, BigDecimal cantidadSolicitada) {
        return new LineaSolicitud(productoId, cantidadSolicitada);
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public BigDecimal getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}
