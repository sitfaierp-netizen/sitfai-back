package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

/**
 * Value Object: Número de Identificación Tributaria (Colombia).
 * Reemplaza al concepto genérico de RUC.
 * Inmutable.
 */
public record Nit(String valor) {
    public Nit {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El NIT no puede ser nulo o vacío");
        }
        String nitLimpio = valor.replaceAll("[^0-9]", "");
        if (nitLimpio.length() < 8 || nitLimpio.length() > 10) {
            throw new IllegalArgumentException("El NIT debe tener entre 8 y 10 dígitos numéricos: " + valor);
        }
        valor = nitLimpio;
    }
}
