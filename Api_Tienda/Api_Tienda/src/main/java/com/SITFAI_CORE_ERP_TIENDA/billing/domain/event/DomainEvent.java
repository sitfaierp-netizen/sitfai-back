package com.SITFAI_CORE_ERP_TIENDA.billing.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato base para los Eventos de Dominio del Bounded Context de Facturación (Billing).
 * <p>
 * Inmutable y puro: sin dependencias de frameworks externos (REGLA-1, REGLA-3, MCP-01).
 */
public interface DomainEvent {

    /**
     * Identificador único del evento para trazabilidad, idempotencia y Event Sourcing (AUD-03).
     */
    UUID eventoId();

    /**
     * Timestamp del instante UTC en que ocurrió el evento de dominio (AUD-01, AUD-03).
     */
    Instant ocurridoEn();
}
