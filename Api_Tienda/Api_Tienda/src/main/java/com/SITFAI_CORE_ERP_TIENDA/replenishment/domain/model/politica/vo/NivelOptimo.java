package com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo;

public record NivelOptimo(int valor) {
    public NivelOptimo {
        if (valor <= 0) {
            throw new IllegalArgumentException("El Nivel Óptimo debe ser mayor a cero");
        }
    }
}
