package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.exception;

/** Excepción lanzada cuando hay un RUC duplicado para la misma empresa (HTTP 409). */
public class RucDuplicadoException extends RuntimeException {
    public RucDuplicadoException(String ruc, String empresaId) {
        super("El RUC '" + ruc + "' ya existe para la empresa: " + empresaId);
    }
}
