package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato base para todos los Eventos de Dominio del Bounded Context de Ventas / POS.
 * <p>
 * Inmutable y puro: sin dependencias de frameworks ni librerías externas (REGLA-1, REGLA-3).
 */
public interface DomainEvent {

    /**
     * Identificador único del evento para trazabilidad e idempotencia.
     */
    UUID eventoId();

    /**
     * Timestamp exacto del momento en que ocurrió el evento de dominio (AUD-03).
     */
    Instant ocurridoEn();
}
