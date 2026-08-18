package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.util.Objects;

/**
 * Value Object: Arqueo de Caja (Resumen financiero inmutable generado al cierre de un Turno / CAJ-05).
 * <p>
 * Representa el balance consolidado de todas las transacciones procesadas durante la sesión de caja.
 * No es editable post-cierre (AUD-04).
 */
public record ArqueoCaja(
        Dinero montoInicial,
        Dinero totalVentas,
        Dinero totalDevoluciones,
        Dinero totalIngresos,
        Dinero totalEgresos,
        Dinero balanceEsperado
) {

    public ArqueoCaja {
        Objects.requireNonNull(montoInicial, "ArqueoCaja: montoInicial es obligatorio.");
        Objects.requireNonNull(totalVentas, "ArqueoCaja: totalVentas es obligatorio.");
        Objects.requireNonNull(totalDevoluciones, "ArqueoCaja: totalDevoluciones es obligatorio.");
        Objects.requireNonNull(totalIngresos, "ArqueoCaja: totalIngresos es obligatorio.");
        Objects.requireNonNull(totalEgresos, "ArqueoCaja: totalEgresos es obligatorio.");
        Objects.requireNonNull(balanceEsperado, "ArqueoCaja: balanceEsperado es obligatorio.");
    }

    public static ArqueoCaja calcular(
            Dinero montoInicial,
            Dinero totalVentas,
            Dinero totalDevoluciones,
            Dinero totalIngresos,
            Dinero totalEgresos) {

        Objects.requireNonNull(montoInicial, "montoInicial es requerido para calcular el arqueo.");
        Objects.requireNonNull(totalVentas, "totalVentas es requerido para calcular el arqueo.");
        Objects.requireNonNull(totalDevoluciones, "totalDevoluciones es requerido para calcular el arqueo.");
        Objects.requireNonNull(totalIngresos, "totalIngresos es requerido para calcular el arqueo.");
        Objects.requireNonNull(totalEgresos, "totalEgresos es requerido para calcular el arqueo.");

        // Balance esperado = Monto Inicial + Ventas + Ingresos - Devoluciones - Egresos
        Dinero balance = montoInicial
                .sumar(totalVentas)
                .sumar(totalIngresos)
                .restar(totalDevoluciones)
                .restar(totalEgresos);

        return new ArqueoCaja(
                montoInicial,
                totalVentas,
                totalDevoluciones,
                totalIngresos,
                totalEgresos,
                balance
        );
    }
}
