package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;

/**
 * Puerto de Salida (Driven Port) para la publicación de Eventos de Dominio de Inventario.
 * Permite a la capa de Aplicación despachar eventos sin acoplarse a tecnologías específicas (Spring, Kafka, etc.).
 */
public interface BodegaEventPublisher {
    
    /**
     * Publica un evento de dominio único.
     */
    void publicar(DomainEvent event);
    
    /**
     * Publica una lista de eventos de dominio de forma iterativa.
     */
    default void publicarTodos(Iterable<DomainEvent> events) {
        if (events != null) {
            events.forEach(this::publicar);
        }
    }
}
