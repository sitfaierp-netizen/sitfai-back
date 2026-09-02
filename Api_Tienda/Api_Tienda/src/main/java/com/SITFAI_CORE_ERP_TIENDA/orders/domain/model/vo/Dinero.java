package com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Dinero(BigDecimal monto) {
    
    public Dinero {
        if (monto == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        monto = monto.setScale(2, RoundingMode.HALF_UP);
    }
    
    public static Dinero zero() {
        return new Dinero(BigDecimal.ZERO);
    }
    
    public Dinero sumar(Dinero otro) {
        return new Dinero(this.monto.add(otro.monto));
    }
    
    public Dinero multiplicar(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        return new Dinero(this.monto.multiply(BigDecimal.valueOf(cantidad)));
    }
}
