package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo;

import java.math.BigDecimal;
import java.util.Objects;

public record Dinero(BigDecimal monto) {
    
    public static final Dinero CERO = new Dinero(BigDecimal.ZERO);

    public Dinero {
        Objects.requireNonNull(monto, "El monto no puede ser nulo.");
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo.");
        }
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(this.monto.add(otro.monto));
    }

    public Dinero multiplicar(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        return new Dinero(this.monto.multiply(BigDecimal.valueOf(cantidad)));
    }
}
