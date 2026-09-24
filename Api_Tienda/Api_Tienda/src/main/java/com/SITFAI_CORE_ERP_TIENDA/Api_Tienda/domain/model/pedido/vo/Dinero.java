package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object: Encapsula un valor monetario inmutable.
 * <p>
 * Regla MONEY-01: Se maneja exclusivamente con {@link BigDecimal} y redondeo {@link RoundingMode#HALF_UP}.
 * Prohibido el uso de float/double para operaciones aritméticas en el dominio.
 * Validación fail-fast: no se admiten montos negativos.
 */
public record Dinero(BigDecimal monto) {

    public static final int ESCALA = 2;
    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);
    public static final String MONEDA_DEFECTO = "USD";

    public Dinero {
        Objects.requireNonNull(monto, "Dinero: el monto no puede ser nulo (MONEY-01).");
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Dinero: el monto no puede ser negativo. Recibido: " + monto);
        }
        monto = monto.setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public static Dinero cero() {
        return CERO;
    }

    public static Dinero de(BigDecimal monto) {
        return new Dinero(monto);
    }

    public static Dinero de(BigDecimal monto, String moneda) {
        return new Dinero(monto);
    }

    public static Dinero de(double valor) {
        return new Dinero(BigDecimal.valueOf(valor));
    }

    public static Dinero de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Dinero: la representación en cadena no puede ser nula ni vacía.");
        }
        return new Dinero(new BigDecimal(valor.trim()));
    }

    public String moneda() {
        return MONEDA_DEFECTO;
    }

    public Dinero sumar(Dinero otro) {
        Objects.requireNonNull(otro, "Dinero a sumar no puede ser nulo.");
        return new Dinero(this.monto.add(otro.monto));
    }

    public Dinero multiplicar(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("El factor de multiplicación no puede ser negativo: " + factor);
        }
        return new Dinero(this.monto.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean esMayorQue(Dinero otro) {
        Objects.requireNonNull(otro, "Dinero a comparar no puede ser nulo.");
        return this.monto.compareTo(otro.monto) > 0;
    }

    @Override
    public String toString() {
        return monto.toPlainString();
    }
}
