package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;

import java.time.Instant;
import java.util.Objects;

/**
 * Resumen financiero inmutable generado durante el arqueo de caja (Reglas CAJ-05 a CAJ-07 y DOC-01).
 * <p>
 * Consolida las transacciones procesadas, el saldo teórico esperado y el descuadre
 * resultante frente al dinero físico contado por el cajero.
 */
public record ArqueoCaja(
        Dinero montoApertura,
        Dinero totalVentas,
        Dinero totalIngresos,
        Dinero totalDevoluciones,
        Dinero totalEgresos,
        Dinero totalTeoricoEsperado,
        Dinero montoFisicoDeclarado,
        Dinero descuadre,
        Instant fechaCierre
) {

    public ArqueoCaja {
        Objects.requireNonNull(montoApertura, "El monto de apertura no puede ser nulo");
        Objects.requireNonNull(totalVentas, "El total de ventas no puede ser nulo");
        Objects.requireNonNull(totalIngresos, "El total de ingresos no puede ser nulo");
        Objects.requireNonNull(totalDevoluciones, "El total de devoluciones no puede ser nulo");
        Objects.requireNonNull(totalEgresos, "El total de egresos no puede ser nulo");
        Objects.requireNonNull(totalTeoricoEsperado, "El total teórico esperado no puede ser nulo");
        Objects.requireNonNull(montoFisicoDeclarado, "El monto físico declarado no puede ser nulo");
        Objects.requireNonNull(descuadre, "El descuadre no puede ser nulo");
        Objects.requireNonNull(fechaCierre, "La fecha de cierre no puede ser nula");
    }

    public static ArqueoCaja calcular(
            Dinero montoApertura,
            Dinero totalVentas,
            Dinero totalIngresos,
            Dinero totalDevoluciones,
            Dinero totalEgresos,
            Dinero montoFisicoDeclarado,
            Instant fechaCierre
    ) {
        Objects.requireNonNull(montoApertura, "montoApertura es obligatorio");
        Objects.requireNonNull(totalVentas, "totalVentas es obligatorio");
        Objects.requireNonNull(totalIngresos, "totalIngresos es obligatorio");
        Objects.requireNonNull(totalDevoluciones, "totalDevoluciones es obligatorio");
        Objects.requireNonNull(totalEgresos, "totalEgresos es obligatorio");
        Objects.requireNonNull(montoFisicoDeclarado, "montoFisicoDeclarado es obligatorio");
        Objects.requireNonNull(fechaCierre, "fechaCierre es obligatoria");

        // Total Teórico Esperado = montoApertura + VENTAS + INGRESOS - DEVOLUCIONES - EGRESOS
        Dinero totalTeorico = montoApertura
                .sumar(totalVentas)
                .sumar(totalIngresos)
                .restar(totalDevoluciones)
                .restar(totalEgresos);

        // Descuadre = montoFisicoDeclarado - totalTeorico (Positivo: sobrante, Negativo: faltante, Cero: exacto)
        Dinero descuadre = montoFisicoDeclarado.restar(totalTeorico);

        return new ArqueoCaja(
                montoApertura,
                totalVentas,
                totalIngresos,
                totalDevoluciones,
                totalEgresos,
                totalTeorico,
                montoFisicoDeclarado,
                descuadre,
                fechaCierre
        );
    }

    public boolean esCuadrado() {
        return descuadre.esCero();
    }

    public boolean tieneSobrante() {
        return descuadre.esPositivo();
    }

    public boolean tieneFaltante() {
        return descuadre.esNegativo();
    }
}
