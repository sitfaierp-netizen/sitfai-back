package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo;

import java.math.BigDecimal;

public record CantidadInsumo(BigDecimal valor) {
    public CantidadInsumo {
        if (valor == null) {
            throw new IllegalArgumentException("La cantidad no puede ser nula");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }
}
