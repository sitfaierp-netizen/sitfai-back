package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;

import java.util.UUID;

/**
 * Value Object inmutable que representa el identificador del Tenant Raíz (Empresa) para IAM.
 * Cumple con la regla MT-01.
 */
public record EmpresaId(UUID valor) {

    public EmpresaId {
        if (valor == null) {
            throw new UsuarioInvalidoException("El identificador de la empresa no puede ser nulo.");
        }
    }

    public static EmpresaId generar() {
        return new EmpresaId(UUID.randomUUID());
    }

    public static EmpresaId de(UUID valor) {
        return new EmpresaId(valor);
    }

    public static EmpresaId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException("El UUID de la empresa no puede ser nulo o vacío.");
        }
        try {
            return new EmpresaId(UUID.fromString(valor.trim()));
        } catch (IllegalArgumentException e) {
            throw new UsuarioInvalidoException("El formato del UUID de la empresa es inválido: " + valor);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
