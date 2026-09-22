package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;

/**
 * Output Port: Despachador de Eventos para el patrón Outbox (AUD-03).
 * <p>
 * Define el contrato puro de salida para la publicación de eventos almacenados
 * hacia los canales de mensajería o bus de eventos de la plataforma.
 */
public interface EventDispatcherPort {

    /**
     * Despacha un evento de dominio almacenado hacia los suscriptores.
     *
     * @param evento Instancia del evento notariado a despachar.
     * @throws Exception Si ocurre un fallo en el canal de comunicación o serialización.
     */
    void despachar(StoredDomainEvent evento) throws Exception;
}
