package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception;

/**
 * Excepción lanzada cuando una operación viola una regla de negocio o invariante del agregado Empresa.
 * (Mapea a HTTP 422 Unprocessable Entity en Infraestructura).
 */
public class EmpresaInvalidaException extends DomainException {

    public EmpresaInvalidaException(String message) {
        super(message);
    }
}
