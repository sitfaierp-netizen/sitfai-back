package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.RucInvalidoException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: Representación inmutable de un Registro Único de Contribuyente (RUC).
 * <p>
 * Invariante: Debe contener exactamente 11 dígitos numéricos.
 * Implementado como record de Java 25.
 */
public record Ruc(String valor) {

    private static final Pattern PATRON_RUC = Pattern.compile("^\\d{11}$");

    public Ruc {
        if (valor == null || valor.isBlank()) {
            throw new RucInvalidoException("RUC no puede ser null ni estar vacío.");
        }
        valor = valor.trim();
        if (!PATRON_RUC.matcher(valor).matches()) {
            throw new RucInvalidoException(
                    String.format("RUC inválido: '%s'. Debe contener exactamente 11 dígitos numéricos.", valor));
        }
    }

    public static Ruc de(String valor) {
        return new Ruc(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
