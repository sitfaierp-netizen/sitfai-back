package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject;

import java.util.Objects;

public record Cantidad(int value) {
    public Cantidad {
        if (value < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
    }

    public static Cantidad de(int value) {
        return new Cantidad(value);
    }
    
    public static Cantidad cero() {
        return new Cantidad(0);
    }

    public Cantidad sumar(Cantidad otra) {
        Objects.requireNonNull(otra);
        return new Cantidad(this.value + otra.value);
    }

    public boolean esMayorQue(Cantidad otra) {
        Objects.requireNonNull(otra);
        return this.value > otra.value;
    }
}
