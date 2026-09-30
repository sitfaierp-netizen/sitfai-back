package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Escribano Global de Auditoría (AUD-03, MT-01).
 * <p>
 * Intercepta todos los eventos de dominio emitidos en el sistema,
 * serializa su payload a JSON y los persiste de manera inmutable en la tabla de Event Store (audit_domain_events).
 */
@Component
public class GlobalDomainEventAuditListener {

    private static final Logger log = LoggerFactory.getLogger(GlobalDomainEventAuditListener.class);

    private final DomainEventAuditJpaRepository repository;
    private final ObjectMapper objectMapper;

    public GlobalDomainEventAuditListener(DomainEventAuditJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = Objects.requireNonNull(repository, "repository no puede ser null");
        this.objectMapper = objectMapper != null
                ? objectMapper.copy().registerModule(new JavaTimeModule())
                : new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onDomainEvent(Object event) {
        if (event == null || isFrameworkEvent(event)) {
            return;
        }

        try {
            String id = extractEventoId(event);
            String eventType = extractEventType(event);
            Instant occurredOn = extractOccurredOn(event);
            String empresaId = extractEmpresaId(event);
            String aggregateId = extractAggregateId(event);
            String payload = objectMapper.writeValueAsString(event);

            int inserted = repository.insertIfAbsent(
                    id,
                    aggregateId,
                    eventType,
                    payload,
                    empresaId,
                    occurredOn
            );

            if (inserted == 0) {
                log.debug("Evento de Dominio ya auditado; se omite replay idempotente: eventType={}, id={}",
                        eventType, id);
            } else {
                log.debug("Evento de Dominio auditado exitosamente: eventType={}, id={}, empresaId={}, aggregateId={}",
                        eventType, id, empresaId, aggregateId);
            }

        } catch (Exception e) {
            log.error("Error al persistir auditoría de evento de dominio [{}]: {}",
                    event.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private boolean isFrameworkEvent(Object event) {
        String packageName = event.getClass().getPackageName();
        return packageName.startsWith("org.springframework")
                || packageName.startsWith("org.hibernate")
                || packageName.startsWith("org.apache");
    }

    private String extractEventoId(Object event) {
        Object val = invokeMethodIfExists(event, "eventoId", "getEventoId", "id", "getId");
        if (val instanceof UUID uuid) {
            return uuid.toString();
        }
        if (val != null) {
            return val.toString();
        }
        return UUID.randomUUID().toString();
    }

    private String extractEventType(Object event) {
        Object val = invokeMethodIfExists(event, "tipoEvento", "getTipoEvento");
        if (val != null && !val.toString().isBlank()) {
            return val.toString();
        }
        return event.getClass().getSimpleName();
    }

    private Instant extractOccurredOn(Object event) {
        Object val = invokeMethodIfExists(event, "ocurridoEn", "getOcurridoEn", "occurredOn", "getOccurredOn");
        if (val instanceof Instant instant) {
            return instant;
        }
        return Instant.now();
    }

    private String extractEmpresaId(Object event) {
        Object val = invokeMethodIfExists(event, "empresaId", "getEmpresaId");
        return extractIdValue(val);
    }

    private String extractAggregateId(Object event) {
        if (event.getClass().isRecord()) {
            for (RecordComponent component : event.getClass().getRecordComponents()) {
                String name = component.getName();
                if (!name.equalsIgnoreCase("eventoId")
                        && !name.equalsIgnoreCase("empresaId")
                        && !name.equalsIgnoreCase("ocurridoEn")
                        && name.toLowerCase().endsWith("id")) {
                    try {
                        Object val = component.getAccessor().invoke(event);
                        String idVal = extractIdValue(val);
                        if (idVal != null) {
                            return idVal;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        for (String methodName : new String[]{"pedidoId", "facturaId", "turnoId", "bodegaId", "usuarioId", "aggregateId"}) {
            Object val = invokeMethodIfExists(event, methodName);
            String idVal = extractIdValue(val);
            if (idVal != null) {
                return idVal;
            }
        }
        return null;
    }

    private String extractIdValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid.toString();
        }
        if (value instanceof String str) {
            return str;
        }
        Object innerVal = invokeMethodIfExists(value, "valor", "getValor", "id", "getId", "value", "getValue");
        if (innerVal != null) {
            return innerVal.toString();
        }
        return value.toString();
    }

    private Object invokeMethodIfExists(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                log.trace("No se pudo invocar método {} en {}: {}", methodName, target.getClass().getSimpleName(), e.getMessage());
            }
        }
        return null;
    }
}
