package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de Integración / Mensaje Outbox publicado al bus de eventos de la aplicación.
 * <p>
 * Transporta el payload JSON deserializado y metadatos de auditoría para consumo inter-módulos.
 */
public record OutboxMessageEvent(
        UUID id,
        UUID empresaId,
        String nombreEvento,
        Instant ocurridoEn,
        String payloadJson,
        JsonNode payload
) {
}
