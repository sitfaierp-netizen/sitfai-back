package com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception;

/**
 * Excepción lanzada ante inconsistencias en la identidad o configuración de la Caja.
 */
public class CajaInvalidaException extends DomainException {

    public CajaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
