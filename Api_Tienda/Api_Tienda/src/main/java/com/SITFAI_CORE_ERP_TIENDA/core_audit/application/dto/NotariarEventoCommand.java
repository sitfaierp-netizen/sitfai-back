package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Command DTO inmutable para la notarización y persistencia de eventos en el Event Store (AUD-03, MT-01).
 * <p>
 * Puro, sin anotaciones de Spring ni JPA (Regla 1 de Arquitectura).
 */
public record NotariarEventoCommand(
        StoredEventId id,
        UUID empresaId,
        String nombreEvento,
        Instant ocurridoEn,
        Object eventoOriginal,
        String payloadJson
) {
    public NotariarEventoCommand {
        Objects.requireNonNull(empresaId, "NotariarEventoCommand: empresaId no puede ser nulo (MT-01).");
        if (nombreEvento == null || nombreEvento.isBlank()) {
            throw new IllegalArgumentException("NotariarEventoCommand: nombreEvento no puede ser nulo ni vacío.");
        }
        if (eventoOriginal == null && (payloadJson == null || payloadJson.isBlank())) {
            throw new IllegalArgumentException("NotariarEventoCommand: debe suministrarse el eventoOriginal o el payloadJson.");
        }
    }

    public static NotariarEventoCommand desdeEvento(UUID empresaId, String nombreEvento, Instant ocurridoEn, Object eventoOriginal) {
        return new NotariarEventoCommand(
                StoredEventId.generar(),
                empresaId,
                nombreEvento,
                ocurridoEn != null ? ocurridoEn : Instant.now(),
                eventoOriginal,
                null
        );
    }

    public static NotariarEventoCommand conPayload(StoredEventId id, UUID empresaId, String nombreEvento, Instant ocurridoEn, String payloadJson) {
        return new NotariarEventoCommand(
                id != null ? id : StoredEventId.generar(),
                empresaId,
                nombreEvento,
                ocurridoEn != null ? ocurridoEn : Instant.now(),
                null,
                payloadJson
        );
    }
}
