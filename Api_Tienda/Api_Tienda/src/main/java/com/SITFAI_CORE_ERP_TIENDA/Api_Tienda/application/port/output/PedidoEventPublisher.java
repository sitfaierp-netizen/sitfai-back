package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;

import java.util.List;

/**
 * Driven Port (SPI): Publicador de Eventos de Dominio drenados desde los Agregados.
 * <p>
 * Permite desacoplar la Capa de Aplicación de la tecnología de eventos subyacente (Spring Events, Kafka, etc.).
 */
public interface PedidoEventPublisher {

    /**
     * Publica un evento de dominio individual.
     */
    void publicar(DomainEvent evento);

    /**
     * Publica una lista de eventos de dominio en lote.
     */
    default void publicarTodos(List<DomainEvent> eventos) {
        if (eventos != null) {
            eventos.forEach(this::publicar);
        }
    }
}
