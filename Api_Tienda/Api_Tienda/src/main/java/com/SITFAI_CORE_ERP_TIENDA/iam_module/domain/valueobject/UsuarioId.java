package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object inmutable que representa el identificador único de un Usuario.
 */
public record UsuarioId(UUID valor) {

    public UsuarioId {
        if (valor == null) {
            throw new UsuarioInvalidoException("El identificador del usuario no puede ser nulo.");
        }
    }

    public static UsuarioId generar() {
        return new UsuarioId(UUID.randomUUID());
    }

    public static UsuarioId de(UUID valor) {
        return new UsuarioId(valor);
    }

    public static UsuarioId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException("El UUID del usuario no puede ser nulo o vacío.");
        }
        try {
            return new UsuarioId(UUID.fromString(valor.trim()));
        } catch (IllegalArgumentException e) {
            throw new UsuarioInvalidoException("El formato del UUID del usuario es inválido: " + valor);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
