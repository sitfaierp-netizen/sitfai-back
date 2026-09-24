package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object: Encapsula las métricas de movimiento logístico de un producto
 * en una bodega, usadas como insumo para el cálculo del Análisis ABC.
 * <p>
 * Las dos dimensiones capturadas son:
 * <ul>
 *   <li><b>frecuenciaSalida</b>: Número de veces que el producto fue despachado
 *       en el período de análisis (entero estrictamente positivo).</li>
 *   <li><b>valorTotalDespachado</b>: Suma monetaria del valor de todos los despachos
 *       del producto en el período (BigDecimal, precisión DECIMAL(19,4), MONEY-01).</li>
 * </ul>
 * <p>
 * Regla REGLA-3: Inmutable — sin setters, sin estado mutable.
 * Regla MONEY-01: Todo valor monetario se maneja con {@code BigDecimal} y {@code RoundingMode.HALF_UP}.
 * Regla REGLA-1: Cero dependencias de frameworks.
 */
public record MetricaMovimiento(int frecuenciaSalida, BigDecimal valorTotalDespachado) {

    private static final int ESCALA_MONETARIA = 4;

    /** Constructor compacto con validación fail-fast. */
    public MetricaMovimiento {
        if (frecuenciaSalida < 0) {
            throw new IllegalArgumentException(
                    "MetricaMovimiento: la frecuenciaSalida no puede ser negativa. Recibido: " + frecuenciaSalida);
        }
        Objects.requireNonNull(valorTotalDespachado,
                "MetricaMovimiento: el valorTotalDespachado no puede ser null (MONEY-01).");
        if (valorTotalDespachado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "MetricaMovimiento: el valorTotalDespachado no puede ser negativo. Recibido: " + valorTotalDespachado);
        }
        // Normalizar la escala monetaria (MONEY-01: DECIMAL(19,4))
        valorTotalDespachado = valorTotalDespachado.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    /**
     * Métrica neutral — representa un producto sin movimientos registrados.
     * Usado en el factory method del Agregado para inicialización en NO_CLASIFICADO.
     */
    public static MetricaMovimiento sinMovimientos() {
        return new MetricaMovimiento(0, BigDecimal.ZERO);
    }

    /**
     * Factory method de conveniencia.
     *
     * @param frecuencia    Número de salidas del período.
     * @param valorTotal    Valor monetario total despachado.
     */
    public static MetricaMovimiento de(int frecuencia, BigDecimal valorTotal) {
        return new MetricaMovimiento(frecuencia, valorTotal);
    }

    /**
     * Indica si este producto tuvo al menos un movimiento de salida en el período.
     */
    public boolean tieneMovimientos() {
        return frecuenciaSalida > 0 || valorTotalDespachado.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return "MetricaMovimiento{frecuencia=" + frecuenciaSalida
                + ", valorDespachado=" + valorTotalDespachado.toPlainString() + "}";
    }
}
