package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.RucInvalidoException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: Registro Único de Contribuyente (RUC) de la Empresa (EMP-02).
 * Formato estricto: Exactamente 11 dígitos numéricos.
 * Inmutable (Record de Java 25).
 */
public record Ruc(String valor) {

    private static final Pattern PATRON_RUC = Pattern.compile("^\\d{11}$");

    public Ruc {
        Objects.requireNonNull(valor, "El valor del RUC no puede ser null.");
        String limpio = valor.trim();
        if (!PATRON_RUC.matcher(limpio).matches()) {
            throw new RucInvalidoException(valor);
        }
        valor = limpio;
    }

    public static Ruc de(String valor) {
        return new Ruc(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
