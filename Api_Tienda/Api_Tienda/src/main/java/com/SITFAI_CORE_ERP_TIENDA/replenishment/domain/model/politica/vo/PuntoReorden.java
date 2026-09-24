package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo;

public record PuntoReorden(int valor) {
    public PuntoReorden {
        if (valor < 0) {
            throw new IllegalArgumentException("El Punto de Reorden no puede ser negativo");
        }
    }
}
