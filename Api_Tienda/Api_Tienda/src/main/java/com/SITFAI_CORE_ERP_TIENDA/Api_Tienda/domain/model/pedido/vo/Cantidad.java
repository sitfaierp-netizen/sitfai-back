package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Representa una cantidad de ítems o productos en el pedido.
 * <p>
 * Inmutable (record Java 25).
 * Invariante: La cantidad debe ser estrictamente positiva (mayor a 0).
 */
public record Cantidad(int valor) {

    public Cantidad {
        if (valor <= 0) {
            throw new IllegalArgumentException("Cantidad: el valor debe ser estrictamente positivo mayor a 0. Recibido: " + valor);
        }
    }

    public static Cantidad de(int valor) {
        return new Cantidad(valor);
    }

    public static Cantidad de(BigDecimal valor) {
        Objects.requireNonNull(valor, "Cantidad: el valor BigDecimal no puede ser null.");
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cantidad: el valor debe ser mayor a 0. Recibido: " + valor);
        }
        return new Cantidad(valor.intValueExact());
    }

    public static Cantidad uno() {
        return new Cantidad(1);
    }

    public Cantidad sumar(Cantidad otra) {
        Objects.requireNonNull(otra, "Cantidad a sumar no puede ser null.");
        return new Cantidad(this.valor + otra.valor);
    }

    public Cantidad sumar(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Cantidad a incrementar debe ser mayor a 0. Recibido: " + unidades);
        }
        return new Cantidad(this.valor + unidades);
    }

    public BigDecimal aBigDecimal() {
        return BigDecimal.valueOf(valor);
    }

    @Override
    public String toString() {
        return String.valueOf(valor);
    }
}
