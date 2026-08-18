package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object inmutable para representar montos monetarios (Costos).
 * Regla de negocio: Todo monto debe redondearse a 4 decimales en contabilidad.
 */
public record Dinero(BigDecimal monto, String moneda) {

    public Dinero(BigDecimal monto, String moneda) {
        this.moneda = (moneda != null && !moneda.isBlank()) ? moneda : "COP";
        this.monto = Objects.requireNonNull(monto, "El monto no puede ser nulo")
                .setScale(4, RoundingMode.HALF_UP);
        if (this.monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO, "COP");
    }

    public static Dinero cero(String moneda) {
        return new Dinero(BigDecimal.ZERO, moneda);
    }

    public static Dinero de(double valor) {
        return new Dinero(BigDecimal.valueOf(valor), "COP");
    }

    public Dinero sumar(Dinero otro) {
        validarMoneda(otro);
        return new Dinero(this.monto.add(otro.monto), this.moneda);
    }

    public Dinero multiplicar(BigDecimal factor) {
        return new Dinero(this.monto.multiply(factor), this.moneda);
    }

    private void validarMoneda(Dinero otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden operar dineros de distintas monedas");
        }
    }
}
