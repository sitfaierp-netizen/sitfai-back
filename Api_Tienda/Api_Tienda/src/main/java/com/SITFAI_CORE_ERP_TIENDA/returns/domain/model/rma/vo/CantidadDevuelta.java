package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo;

public record CantidadDevuelta(int valor) {
    public CantidadDevuelta {
        if (valor <= 0) {
            throw new IllegalArgumentException("La cantidad devuelta debe ser mayor a cero");
        }
    }
    public static CantidadDevuelta de(int cantidad) {
        return new CantidadDevuelta(cantidad);
    }
}
