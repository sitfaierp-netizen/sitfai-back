package com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception;

/**
 * Excepción lanzada ante cualquier violación de invariantes en el Agregado TurnoCaja
 * (e.g. monto de apertura negativo, operar en turno cerrado, re-cierre de turno).
 */
public class TurnoInvalidoException extends DomainException {

    public TurnoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
