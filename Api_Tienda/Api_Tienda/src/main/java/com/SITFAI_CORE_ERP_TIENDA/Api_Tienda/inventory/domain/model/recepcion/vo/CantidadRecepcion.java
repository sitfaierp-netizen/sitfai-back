package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo;

public record CantidadRecepcion(int valor) {
    public CantidadRecepcion {
        if (valor < 0) {
            throw new IllegalArgumentException("CantidadRecepcion: no puede ser negativa.");
        }
    }
    
    public CantidadRecepcion sumar(CantidadRecepcion otra) {
        return new CantidadRecepcion(this.valor + otra.valor());
    }
}
