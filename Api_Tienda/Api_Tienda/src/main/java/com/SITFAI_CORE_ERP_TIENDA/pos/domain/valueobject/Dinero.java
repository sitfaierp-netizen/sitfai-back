package com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Dinero(BigDecimal valor) {
    public Dinero {
        Objects.requireNonNull(valor, "El valor monetario no puede ser nulo");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor monetario no puede ser negativo");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static Dinero de(BigDecimal valor) {
        return new Dinero(valor);
    }
    
    public static Dinero de(String valor) {
        return new Dinero(new BigDecimal(valor));
    }

    public static Dinero cero() {
        return new Dinero(BigDecimal.ZERO);
    }

    public Dinero sumar(Dinero otro) {
        Objects.requireNonNull(otro);
        return new Dinero(this.valor.add(otro.valor));
    }
    
    public Dinero restar(Dinero otro) {
        Objects.requireNonNull(otro);
        if (this.valor.compareTo(otro.valor) < 0) {
            throw new IllegalArgumentException("El resultado no puede ser negativo");
        }
        return new Dinero(this.valor.subtract(otro.valor));
    }
}
