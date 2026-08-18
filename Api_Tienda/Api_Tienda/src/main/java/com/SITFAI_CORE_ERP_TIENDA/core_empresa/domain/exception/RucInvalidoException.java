package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception;

/**
 * Excepción lanzada cuando un RUC no cumple con el formato oficial de 11 dígitos numéricos (EMP-02).
 */
public class RucInvalidoException extends DomainException {

    public RucInvalidoException(String ruc) {
        super("El RUC '" + ruc + "' no es válido. Debe tener exactamente 11 dígitos numéricos.");
    }
}
