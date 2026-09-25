package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo;

public record CantidadProducir(Integer valor) {
    public CantidadProducir {
        if (valor == null) {
            throw new IllegalArgumentException("La cantidad a producir no puede ser nula");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("La cantidad a producir debe ser mayor a cero");
        }
    }
}
