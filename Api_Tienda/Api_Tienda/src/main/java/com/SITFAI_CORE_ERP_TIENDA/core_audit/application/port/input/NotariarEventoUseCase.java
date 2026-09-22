package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.NotariarEventoCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;

/**
 * Driving Port / Caso de Uso: Notarización inmutable de eventos de dominio en el Event Store (AUD-03, MT-01).
 */
public interface NotariarEventoUseCase {

    /**
     * Orquesta la recepción, serialización a JSON y persistencia inmutable del evento de dominio.
     *
     * @param command Comando con los datos del evento y contexto del tenant.
     * @return Evento almacenado en estado PENDIENTE.
     */
    StoredDomainEvent notariarEvento(NotariarEventoCommand command);
}
