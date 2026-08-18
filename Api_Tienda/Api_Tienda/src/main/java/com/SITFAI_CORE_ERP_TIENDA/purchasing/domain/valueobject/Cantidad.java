package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

import java.math.BigDecimal;

public record Cantidad(BigDecimal value) {
    public Cantidad {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }

    public static Cantidad of(String val) {
        return new Cantidad(new BigDecimal(val));
    }

    public static Cantidad of(int val) {
        return new Cantidad(BigDecimal.valueOf(val));
    }
}
