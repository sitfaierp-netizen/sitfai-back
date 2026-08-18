package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;

import java.util.regex.Pattern;

/**
 * Value Object inmutable que representa el nombre de usuario único en IAM.
 * Invariantes: Alfanumérico (letras, dígitos, ., _, -), longitud entre 4 y 50 caracteres.
 */
public record Username(String valor) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{4,50}$");

    public Username {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException("El nombre de usuario (username) no puede ser nulo o vacío.");
        }
        valor = valor.trim();
        if (!USERNAME_PATTERN.matcher(valor).matches()) {
            throw new UsuarioInvalidoException(
                    "El username '" + valor + "' es inválido. Debe ser alfanumérico (puede incluir '.', '_', '-') y tener entre 4 y 50 caracteres."
            );
        }
    }

    public static Username de(String valor) {
        return new Username(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
