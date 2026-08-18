package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Impuesto (IVA, RETEFUENTE, etc).
 * Contiene tarifa y valor calculado. Inmutable.
 */
public record Impuesto(String tipoImpuesto, BigDecimal tarifa, Dinero valor) {
    public Impuesto {
        Objects.requireNonNull(tipoImpuesto, "El tipo de impuesto no puede ser nulo");
        Objects.requireNonNull(tarifa, "La tarifa no puede ser nula");
        Objects.requireNonNull(valor, "El valor del impuesto no puede ser nulo");
        
        if (tarifa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa");
        }
    }
    
    public static Impuesto calcular(String tipoImpuesto, BigDecimal tarifa, Dinero base) {
        Dinero valorCalculado = base.multiplicar(tarifa).multiplicar(new BigDecimal("0.01")); // tarifa en porcentaje (e.g., 19)
        return new Impuesto(tipoImpuesto, tarifa, valorCalculado);
    }
}
