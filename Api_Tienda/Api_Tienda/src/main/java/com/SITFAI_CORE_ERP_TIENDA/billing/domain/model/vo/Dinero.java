package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo;

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
        monto = monto.setScale(4, RoundingMode.HALF_UP);
    }
    
    public Dinero sumar(Dinero otro) {
        return new Dinero(this.monto.add(otro.monto()));
    }

    public Dinero restar(Dinero otro) {
        return new Dinero(this.monto.subtract(otro.monto()));
    }

    public Dinero multiplicar(int factor) {
        return new Dinero(this.monto.multiply(BigDecimal.valueOf(factor)));
    }
    
    public Dinero multiplicar(BigDecimal factor) {
        return new Dinero(this.monto.multiply(factor));
    }
}
