package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.listener;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.NotariarEventoCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input.NotariarEventoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * El Gran Oyente (Global Listener) de Auditoría y Trazabilidad (AUD-03, MT-01).
 * <p>
 * Intercepta cualquier evento de dominio emitido en la plataforma usando
 * {@code @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)}
 * asegurando que los eventos queden sellados en la misma transacción antes del commit (Outbox Pattern).
 */
@Component("coreAuditGlobalDomainEventAuditListener")
public class GlobalDomainEventAuditListener {

    private static final Logger log = LoggerFactory.getLogger(GlobalDomainEventAuditListener.class);

    private final NotariarEventoUseCase notariarEventoUseCase;

    public GlobalDomainEventAuditListener(NotariarEventoUseCase notariarEventoUseCase) {
        this.notariarEventoUseCase = Objects.requireNonNull(notariarEventoUseCase, "notariarEventoUseCase no puede ser null");
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onDomainEvent(Object event) {
        if (event == null || isFrameworkEvent(event)) {
            return;
        }

        if (!isDomainEvent(event)) {
            return;
        }

        try {
            UUID empresaId = extractEmpresaId(event);
            String nombreEvento = event.getClass().getSimpleName();
            Instant ocurridoEn = extractOccurredOn(event);

            NotariarEventoCommand command = new NotariarEventoCommand(
                    StoredEventId.generar(),
                    empresaId,
                    nombreEvento,
                    ocurridoEn,
                    event,
                    null
            );

            notariarEventoUseCase.notariarEvento(command);
            log.info("Evento auditado exitosamente en Event Store: evento={}, empresaId={}", nombreEvento, empresaId);

        } catch (Exception e) {
            log.error("Error al notariar evento de dominio [{}] en el Event Store: {}",
                    event.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    /**
     * Determina si el objeto representa un Domain Event en el ecosistema SITFAI.
     */
    public boolean isDomainEvent(Object event) {
        if (event == null) return false;

        String packageName = event.getClass().getPackageName();
        if (packageName.contains(".core_audit")) {
            return false;
        }

        // 1. Interfaces que se llamen DomainEvent
        for (Class<?> iface : event.getClass().getInterfaces()) {
            if (iface.getSimpleName().equals("DomainEvent")) {
                return true;
            }
        }

        // 2. Clases dentro de paquetes .domain.event o nombradas con sufijo Event
        if (packageName.contains(".domain.event") || packageName.contains(".event")) {
            return true;
        }

        return event.getClass().getSimpleName().endsWith("Event");
    }

    private boolean isFrameworkEvent(Object event) {
        String packageName = event.getClass().getPackageName();
        return packageName.startsWith("org.springframework")
                || packageName.startsWith("org.hibernate")
                || packageName.startsWith("org.apache")
                || packageName.startsWith("com.zaxxer");
    }

    /**
     * Extrae de forma genérica y resiliente el EmpresaId (MT-01) del evento.
     */
    public UUID extractEmpresaId(Object event) {
        Object val = invokeMethodIfExists(event, "empresaId", "getEmpresaId");
        if (val == null && event.getClass().isRecord()) {
            for (RecordComponent component : event.getClass().getRecordComponents()) {
                if (component.getName().equalsIgnoreCase("empresaId")) {
                    try {
                        val = component.getAccessor().invoke(event);
                        break;
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (val instanceof UUID uuid) {
            return uuid;
        }

        if (val != null) {
            // Manejo si es un Value Object EmpresaId(UUID valor) o EmpresaId(UUID value)
            try {
                Object nested = invokeMethodIfExists(val, "valor", "value", "getValor", "getValue");
                if (nested instanceof UUID nestedUuid) {
                    return nestedUuid;
                }
            } catch (Exception ignored) {
            }

            // Manejo por string
            try {
                return UUID.fromString(val.toString().trim());
            } catch (Exception ignored) {
            }
        }

        // Fallback defensivo para eventos sin tenant explícito
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    private Instant extractOccurredOn(Object event) {
        Object val = invokeMethodIfExists(event, "ocurridoEn", "getOcurridoEn", "occurredOn", "getOccurredOn", "emitidoEn", "getEmitidoEn");
        if (val instanceof Instant instant) {
            return instant;
        }
        return Instant.now();
    }

    private Object invokeMethodIfExists(Object target, String... methodNames) {
        if (target == null) return null;
        for (String name : methodNames) {
            try {
                Method method = target.getClass().getMethod(name);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                log.trace("No se pudo invocar método {}: {}", name, e.getMessage());
            }
        }
        return null;
    }
}
