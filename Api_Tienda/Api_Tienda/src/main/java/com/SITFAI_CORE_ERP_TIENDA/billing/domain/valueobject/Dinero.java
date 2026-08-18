package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object: Representación monetaria inmutable (Monto + Moneda).
 * <p>
 * Inmutable por diseño (record Java 25).
 * Encapsula la escala fija a 2 decimales y previene inconsistencias de divisas.
 */
public record Dinero(BigDecimal monto, String moneda) {

    public static final String MONEDA_POR_DEFECTO = "COP";
    public static final int ESCALA = 2;
    public static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_UP;

    public Dinero {
        if (monto == null) {
            throw new IllegalArgumentException("Dinero: el monto no puede ser null.");
        }
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Dinero: el monto no puede ser negativo. Recibido: " + monto);
        }
        if (moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("Dinero: el código de moneda no puede ser null ni vacío.");
        }
        monto = monto.setScale(ESCALA, MODO_REDONDEO);
        moneda = moneda.trim().toUpperCase();
    }

    public static Dinero de(BigDecimal monto, String moneda) {
        return new Dinero(monto, moneda);
    }

    public static Dinero de(BigDecimal monto) {
        return new Dinero(monto, MONEDA_POR_DEFECTO);
    }

    public static Dinero de(double monto, String moneda) {
        return new Dinero(BigDecimal.valueOf(monto), moneda);
    }

    public static Dinero de(double monto) {
        return new Dinero(BigDecimal.valueOf(monto), MONEDA_POR_DEFECTO);
    }

    public static Dinero cero(String moneda) {
        return new Dinero(BigDecimal.ZERO, moneda);
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO, MONEDA_POR_DEFECTO);
    }

    public Dinero sumar(Dinero otro) {
        Objects.requireNonNull(otro, "Dinero a sumar no puede ser null.");
        validarMismaMoneda(otro);
        return new Dinero(this.monto.add(otro.monto), this.moneda);
    }

    public Dinero restar(Dinero otro) {
        Objects.requireNonNull(otro, "Dinero a restar no puede ser null.");
        validarMismaMoneda(otro);
        BigDecimal nuevoMonto = this.monto.subtract(otro.monto);
        if (nuevoMonto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Dinero: la resta no puede resultar en un monto negativo.");
        }
        return new Dinero(nuevoMonto, this.moneda);
    }

    public Dinero multiplicar(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("Dinero: la cantidad para multiplicar no puede ser negativa.");
        }
        return new Dinero(this.monto.multiply(BigDecimal.valueOf(cantidad)), this.moneda);
    }

    public Dinero multiplicar(BigDecimal factor) {
        Objects.requireNonNull(factor, "Factor de multiplicación no puede ser null.");
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Dinero: el factor no puede ser negativo.");
        }
        return new Dinero(this.monto.multiply(factor), this.moneda);
    }

    public boolean esCero() {
        return this.monto.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean esMayorQue(Dinero otro) {
        Objects.requireNonNull(otro, "Dinero a comparar no puede ser null.");
        validarMismaMoneda(otro);
        return this.monto.compareTo(otro.monto) > 0;
    }

    private void validarMismaMoneda(Dinero otro) {
        if (!this.moneda.equalsIgnoreCase(otro.moneda)) {
            throw new IllegalArgumentException(
                    String.format("Monedas incompatibles: no se puede operar '%s' con '%s'", this.moneda, otro.moneda));
        }
    }

    @Override
    public String toString() {
        return moneda + " " + monto.toPlainString();
    }
}
