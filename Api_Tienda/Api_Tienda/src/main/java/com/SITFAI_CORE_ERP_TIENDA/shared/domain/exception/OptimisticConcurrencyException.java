package com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception;

/**
 * Excepción de Dominio lanzada cuando falla un chequeo de concurrencia optimista
 * indicando que la versión esperada no coincide con la versión persistida.
 */
public class OptimisticConcurrencyException extends RuntimeException {
    public OptimisticConcurrencyException(String aggregateName, Object id) {
        super(String.format("Conflicto de concurrencia: El agregado %s con ID %s ha sido modificado por otra transacción. La versión esperada no coincide con la versión persistida.", aggregateName, id));
    }
}
