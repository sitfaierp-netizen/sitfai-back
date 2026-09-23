package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object para la manipulación inmutable de importes monetarios en el TPV (Regla MONEY-01).
 * <p>
 * Todas las operaciones aritméticas garantizan redondeo bancario {@link RoundingMode#HALF_UP}
 * y una precisión de 4 decimales según el estándar contable de la plataforma.
 */
public record Dinero(BigDecimal monto) implements Comparable<Dinero> {

    public static final RoundingMode MODO_REDONDEO = RoundingMode.HALF_UP;
    public static final int ESCALA = 4;
    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

    public Dinero {
        Objects.requireNonNull(monto, "El monto monetario no puede ser nulo");
        monto = monto.setScale(ESCALA, MODO_REDONDEO);
    }

    public static Dinero de(BigDecimal monto) {
        return new Dinero(monto);
    }

    public static Dinero de(String monto) {
        Objects.requireNonNull(monto, "El monto en texto no puede ser nulo");
        return new Dinero(new BigDecimal(monto));
    }

    public static Dinero de(long monto) {
        return new Dinero(BigDecimal.valueOf(monto));
    }

    public static Dinero de(double monto) {
        return new Dinero(BigDecimal.valueOf(monto));
    }

    public static Dinero cero() {
        return CERO;
    }

    public Dinero sumar(Dinero otro) {
        Objects.requireNonNull(otro, "El operando no puede ser nulo");
        return new Dinero(this.monto.add(otro.monto));
    }

    public Dinero restar(Dinero otro) {
        Objects.requireNonNull(otro, "El operando no puede ser nulo");
        return new Dinero(this.monto.subtract(otro.monto));
    }

    public Dinero multiplicar(BigDecimal factor) {
        Objects.requireNonNull(factor, "El factor no puede ser nulo");
        return new Dinero(this.monto.multiply(factor).setScale(ESCALA, MODO_REDONDEO));
    }

    public Dinero dividir(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "El divisor no puede ser nulo");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("División por cero en operación de Dinero");
        }
        return new Dinero(this.monto.divide(divisor, ESCALA, MODO_REDONDEO));
    }

    public boolean esPositivo() {
        return this.monto.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean esNegativo() {
        return this.monto.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean esCero() {
        return this.monto.compareTo(BigDecimal.ZERO) == 0;
    }

    public Dinero valorAbsoluto() {
        return new Dinero(this.monto.abs());
    }

    @Override
    public int compareTo(Dinero o) {
        Objects.requireNonNull(o, "No se puede comparar Dinero con nulo");
        return this.monto.compareTo(o.monto);
    }

    @Override
    public String toString() {
        return monto.toPlainString();
    }
}
