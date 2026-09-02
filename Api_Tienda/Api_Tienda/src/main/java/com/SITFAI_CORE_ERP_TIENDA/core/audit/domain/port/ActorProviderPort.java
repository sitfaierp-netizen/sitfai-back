package com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port;

public interface ActorProviderPort {
    /**
     * Devuelve el ID (ej. subject de Keycloak, username) del actor que está ejecutando la acción actual.
     * @return String que representa el ID del actor, o un valor por defecto (ej. "system") si no hay contexto.
     */
    String getCurrentActorId();
}
