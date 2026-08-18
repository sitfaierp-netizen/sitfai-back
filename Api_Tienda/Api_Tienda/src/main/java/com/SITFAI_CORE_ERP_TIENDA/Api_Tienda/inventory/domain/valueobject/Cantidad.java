package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject;

import java.math.BigDecimal;

/**
 * Value Object: Cantidad de unidades involucradas en un movimiento de inventario.
 * <p>
 * Siempre representa un valor POSITIVO mayor que cero. La dirección del movimiento
 * (entrada o salida) es responsabilidad del {@code TipoMovimiento} en la Entidad
 * {@code MovimientoInventario}, no de esta cantidad.
 * <p>
 * Usa {@code BigDecimal} para soporte de unidades fraccionarias (kg, litros, etc.)
 * con precisión contable.
 * <p>
 * Reglas validadas: BOD-05 (parte de la invariante), MCP-01.
 */
public record Cantidad(BigDecimal valor) {

    /**
     * Constructor compacto — validación fail-fast.
     * La cantidad siempre debe ser estrictamente positiva.
     */
    public Cantidad {
        if (valor == null) {
            throw new IllegalArgumentException("Cantidad: el valor no puede ser null.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Cantidad: el valor debe ser estrictamente positivo. Valor recibido: " + valor);
        }
    }

    /**
     * Factory method de conveniencia.
     */
    public static Cantidad de(BigDecimal valor) {
        return new Cantidad(valor);
    }

    /**
     * Factory method de conveniencia desde entero (unidades enteras).
     */
    public static Cantidad de(int unidades) {
        return new Cantidad(BigDecimal.valueOf(unidades));
    }

    /**
     * Suma dos cantidades — devuelve una nueva instancia (inmutabilidad).
     */
    public Cantidad sumar(Cantidad otra) {
        return new Cantidad(this.valor.add(otra.valor));
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
