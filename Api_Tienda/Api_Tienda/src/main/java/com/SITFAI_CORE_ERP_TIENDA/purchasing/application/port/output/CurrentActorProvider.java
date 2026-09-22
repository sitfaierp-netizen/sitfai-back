package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output;

/**
 * Driven / Output Port: Obtiene el identificador del usuario o actor actual que ejecuta la operación.
 * <p>
 * Regla AUD-01: Auditoría de operaciones críticas mediante trazabilidad del actor.
 */
public interface CurrentActorProvider {
    String getActorActual();
}
