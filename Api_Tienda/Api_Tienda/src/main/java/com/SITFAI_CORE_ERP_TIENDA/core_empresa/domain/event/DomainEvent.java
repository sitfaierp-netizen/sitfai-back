package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato base para todos los Eventos de Dominio del Bounded Context core-empresa.
 */
public interface DomainEvent {

    UUID eventoId();

    Instant ocurridoEn();

    default String tipoEvento() {
        return getClass().getSimpleName();
    }
}
