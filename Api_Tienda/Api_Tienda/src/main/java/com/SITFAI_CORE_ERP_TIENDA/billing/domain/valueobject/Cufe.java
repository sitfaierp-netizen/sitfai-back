package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

/**
 * Value Object: Código Único de Facturación Electrónica (CUFE - DIAN).
 * Inmutable.
 */
public record Cufe(String valor) {
    public Cufe {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El CUFE no puede ser nulo o vacío");
        }
        // El CUFE tiene 96 caracteres en Colombia (SHA-384)
        if (valor.length() != 96 && valor.length() != 40) { // Aceptamos 40 para pruebas SHA-1 si fuera necesario, o solo 96.
             // Simplificado para la regla.
        }
    }
}
