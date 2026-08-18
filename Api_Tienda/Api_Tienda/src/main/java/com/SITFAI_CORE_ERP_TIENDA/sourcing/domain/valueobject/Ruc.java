package com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject;

/**
 * Value Object inmutable que representa el Registro Único de Contribuyente (RUC) 
 * o equivalente de identificación fiscal del Proveedor.
 */
public record Ruc(String valor) {
    public Ruc {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El RUC no puede ser nulo ni vacío.");
        }
    }
    @Override public String toString() { return valor; }
}
