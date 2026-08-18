package com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception;

/**
 * Excepción lanzada cuando un RUC no cumple con el formato requerido de 11 dígitos numéricos.
 */
public class RucInvalidoException extends DomainException {

    public RucInvalidoException(String mensaje) {
        super(mensaje);
    }
}
