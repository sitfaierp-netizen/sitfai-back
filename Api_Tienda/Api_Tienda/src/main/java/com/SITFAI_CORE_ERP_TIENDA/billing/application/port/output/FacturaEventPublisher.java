package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.DomainEvent;

/**
 * Driven Port: Publicador de Eventos de Dominio.
 */
public interface FacturaEventPublisher {
    void publicar(DomainEvent event);
}
