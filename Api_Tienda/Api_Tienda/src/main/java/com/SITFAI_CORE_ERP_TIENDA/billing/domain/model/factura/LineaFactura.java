package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Impuesto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna protegida del Agregado {@link Factura}.
 * <p>
 * Encapsula la descripción del concepto facturado, cantidad, precio unitario e impuestos aplicables.
 * Todas las validaciones son fail-fast (REGLA-3, MONEY-01).
 */
public class LineaFactura {

    private final UUID id;
    private final String descripcion;
    private final BigDecimal cantidad;
    private final Dinero precioUnitario;
    private final List<Impuesto> impuestos;

    public LineaFactura(UUID id, String descripcion, BigDecimal cantidad, Dinero precioUnitario, List<Impuesto> impuestos) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("LineaFactura: la descripción no puede ser nula ni vacía.");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("LineaFactura: la cantidad debe ser mayor a cero. Recibido: " + cantidad);
        }
        if (precioUnitario == null || precioUnitario.monto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("LineaFactura: el precio unitario no puede ser negativo.");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.descripcion = descripcion.trim();
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.impuestos = impuestos != null ? new ArrayList<>(impuestos) : new ArrayList<>();
    }

    public LineaFactura(String descripcion, BigDecimal cantidad, Dinero precioUnitario) {
        this(UUID.randomUUID(), descripcion, cantidad, precioUnitario, new ArrayList<>());
    }

    public LineaFactura(String descripcion, BigDecimal cantidad, Dinero precioUnitario, List<Impuesto> impuestos) {
        this(UUID.randomUUID(), descripcion, cantidad, precioUnitario, impuestos);
    }

    public static LineaFactura crear(String descripcion, BigDecimal cantidad, Dinero precioUnitario) {
        return new LineaFactura(descripcion, cantidad, precioUnitario);
    }

    public static LineaFactura crear(String descripcion, BigDecimal cantidad, Dinero precioUnitario, List<Impuesto> impuestos) {
        return new LineaFactura(descripcion, cantidad, precioUnitario, impuestos);
    }

    public void agregarImpuesto(Impuesto impuesto) {
        Objects.requireNonNull(impuesto, "LineaFactura: impuesto no puede ser null.");
        this.impuestos.add(impuesto);
    }

    public void agregarImpuesto(String tipoImpuesto, BigDecimal tarifa) {
        Impuesto impuesto = Impuesto.calcular(tipoImpuesto, tarifa, calcularSubtotal());
        this.impuestos.add(impuesto);
    }

    public Dinero calcularSubtotal() {
        return precioUnitario.multiplicar(cantidad);
    }

    public Dinero calcularTotalImpuestos() {
        if (impuestos.isEmpty()) {
            return Dinero.cero(precioUnitario.moneda());
        }
        return impuestos.stream()
                .map(Impuesto::valor)
                .reduce(Dinero.cero(precioUnitario.moneda()), Dinero::sumar);
    }

    public Dinero calcularTotal() {
        return calcularSubtotal().sumar(calcularTotalImpuestos());
    }

    public UUID getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public Dinero getPrecioUnitario() {
        return precioUnitario;
    }

    public List<Impuesto> getImpuestos() {
        return Collections.unmodifiableList(impuestos);
    }
}
