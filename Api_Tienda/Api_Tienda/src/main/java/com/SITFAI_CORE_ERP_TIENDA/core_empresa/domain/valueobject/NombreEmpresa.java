package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;

import java.util.Objects;

/**
 * Value Object: Razón Social / Nombre Comercial de la Empresa.
 * Inmutable (Record de Java 25).
 */
public record NombreEmpresa(String valor) {

    public NombreEmpresa {
        Objects.requireNonNull(valor, "El nombre de la empresa no puede ser null.");
        String limpio = valor.trim();
        if (limpio.isBlank()) {
            throw new EmpresaInvalidaException("La razón social o nombre de la empresa no puede estar en blanco.");
        }
        if (limpio.length() < 2 || limpio.length() > 150) {
            throw new EmpresaInvalidaException("El nombre de la empresa debe tener entre 2 y 150 caracteres.");
        }
        valor = limpio;
    }

    public static NombreEmpresa de(String valor) {
        return new NombreEmpresa(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
