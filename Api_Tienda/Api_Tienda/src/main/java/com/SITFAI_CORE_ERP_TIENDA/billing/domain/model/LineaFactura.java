package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Impuesto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity: Línea de detalle de la factura.
 * Encapsula la cantidad, precio unitario y sus propios impuestos.
 */
public class LineaFactura {
    private final UUID id;
    private final String concepto;
    private final BigDecimal cantidad;
    private final Dinero precioUnitario;
    private final List<Impuesto> impuestos;

    /** Constructor principal (creación) */
    public LineaFactura(String concepto, BigDecimal cantidad, Dinero precioUnitario) {
        this(UUID.randomUUID(), concepto, cantidad, precioUnitario);
    }

    /** Constructor de reconstitución desde persistencia */
    public LineaFactura(UUID id, String concepto, BigDecimal cantidad, Dinero precioUnitario) {
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto de la línea es obligatorio");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (precioUnitario == null || precioUnitario.monto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.impuestos = new ArrayList<>();
    }

    public void agregarImpuesto(String tipo, BigDecimal tarifa) {
        Impuesto impuesto = Impuesto.calcular(tipo, tarifa, calcularSubtotal());
        this.impuestos.add(impuesto);
    }

    public Dinero calcularSubtotal() {
        return precioUnitario.multiplicar(cantidad);
    }

    public Dinero calcularTotalImpuestos() {
        return impuestos.stream()
                .map(Impuesto::valor)
                .reduce(Dinero.cero(precioUnitario.moneda()), Dinero::sumar);
    }

    public Dinero calcularTotal() {
        return calcularSubtotal().sumar(calcularTotalImpuestos());
    }

    public UUID getId() { return id; }
    public String getConcepto() { return concepto; }
    public BigDecimal getCantidad() { return cantidad; }
    public Dinero getPrecioUnitario() { return precioUnitario; }
    public List<Impuesto> getImpuestos() { return Collections.unmodifiableList(impuestos); }
}
