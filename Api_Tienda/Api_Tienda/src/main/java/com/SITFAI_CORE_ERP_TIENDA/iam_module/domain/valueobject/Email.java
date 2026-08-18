package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;

import java.util.regex.Pattern;

/**
 * Value Object inmutable que representa la dirección de correo electrónico corporativo de un Usuario.
 */
public record Email(String valor) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public Email {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException("El correo electrónico no puede ser nulo o vacío.");
        }
        valor = valor.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(valor).matches()) {
            throw new UsuarioInvalidoException("El formato del correo electrónico es inválido: " + valor);
        }
    }

    public static Email de(String valor) {
        return new Email(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
