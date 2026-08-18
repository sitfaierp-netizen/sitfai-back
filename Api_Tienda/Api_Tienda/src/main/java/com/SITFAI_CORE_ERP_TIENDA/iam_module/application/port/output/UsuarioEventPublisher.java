package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.DomainEvent;

import java.util.List;

/**
 * Driven Port / Output Port: Publicador de Eventos de Dominio emitidos por el módulo IAM.
 */
public interface UsuarioEventPublisher {
    void publicar(DomainEvent evento);
    void publicarTodos(List<DomainEvent> eventos);
}
