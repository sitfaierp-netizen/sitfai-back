package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProductoId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Entidad interna protegida del Agregado {@link OrdenCompra}.
 * <p>
 * Representa una posición o ítem de abastecimiento solicitado al proveedor.
 */
public class LineaOrdenCompra {

    private final ProductoId productoId;
    private final int cantidadSolicitada;
    private final BigDecimal costoUnitarioEsperado;
    private final BigDecimal subtotalEsperado;

    private LineaOrdenCompra(ProductoId productoId, int cantidadSolicitada, BigDecimal costoUnitarioEsperado) {
        this.productoId = Objects.requireNonNull(productoId, "LineaOrdenCompra: productoId no puede ser nulo.");
        if (cantidadSolicitada <= 0) {
            throw new IllegalArgumentException("LineaOrdenCompra: la cantidad solicitada debe ser estrictamente mayor a cero.");
        }
        this.cantidadSolicitada = cantidadSolicitada;
        this.costoUnitarioEsperado = Objects.requireNonNull(costoUnitarioEsperado, "LineaOrdenCompra: costoUnitarioEsperado no puede ser nulo.");
        if (costoUnitarioEsperado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("LineaOrdenCompra: el costo unitario esperado no puede ser negativo.");
        }
        this.subtotalEsperado = costoUnitarioEsperado
                .multiply(BigDecimal.valueOf(cantidadSolicitada))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public static LineaOrdenCompra crear(ProductoId productoId, int cantidadSolicitada, BigDecimal costoUnitarioEsperado) {
        return new LineaOrdenCompra(productoId, cantidadSolicitada, costoUnitarioEsperado);
    }

    public ProductoId getProductoId() {
        return productoId;
    }

    public int getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public BigDecimal getCostoUnitarioEsperado() {
        return costoUnitarioEsperado;
    }

    public BigDecimal getSubtotalEsperado() {
        return subtotalEsperado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaOrdenCompra that)) return false;
        return cantidadSolicitada == that.cantidadSolicitada
                && Objects.equals(productoId, that.productoId)
                && Objects.equals(costoUnitarioEsperado, that.costoUnitarioEsperado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId, cantidadSolicitada, costoUnitarioEsperado);
    }

    @Override
    public String toString() {
        return "LineaOrdenCompra{" +
                "productoId=" + productoId +
                ", cantidadSolicitada=" + cantidadSolicitada +
                ", costoUnitarioEsperado=" + costoUnitarioEsperado +
                ", subtotalEsperado=" + subtotalEsperado +
                '}';
    }
}
