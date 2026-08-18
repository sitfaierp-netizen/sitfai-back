package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.event.DomainEvent;

/**
 * Driven Port: Publicación de Domain Events del Catálogo.
 * Implementado por SpringEventCatalogPublisher en Infrastructure
 * usando @ApplicationEventPublisher (coreografía intra-proceso, AUD-03).
 */
public interface CatalogEventPublisherPort {
    void publicar(DomainEvent evento);
}
