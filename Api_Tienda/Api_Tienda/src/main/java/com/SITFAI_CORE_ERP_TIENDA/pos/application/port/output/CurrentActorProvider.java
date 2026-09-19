package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output;

/**
 * Puerto de salida para obtener el usuario que ejecuta la acción.
 * Garantiza la trazabilidad (AUD-01) sin confiar en el cliente.
 */
public interface CurrentActorProvider {
    String getActorActual();
}
