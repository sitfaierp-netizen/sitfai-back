package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.poller;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosPendientesCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input.ProcesarEventosPendientesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Motor Cron: Poller del patrón Outbox (AUD-03).
 * <p>
 * Invoca periódicamente al caso de uso {@link ProcesarEventosPendientesUseCase}
 * para recuperar eventos en estado PENDIENTE y asegurar su entrega garantizada (At-least-once delivery).
 */
@Component
public class OutboxEventPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPoller.class);

    private final ProcesarEventosPendientesUseCase useCase;

    public OutboxEventPoller(ProcesarEventosPendientesUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase no puede ser null");
    }

    /**
     * Tarea programada: Ejecuta periódicamente cada 5 segundos (configurable por property).
     */
    @Scheduled(
            fixedDelayString = "${outbox.poller.delay-ms:5000}",
            initialDelayString = "${outbox.poller.initial-delay-ms:2000}"
    )
    public void procesarEventosOutbox() {
        try {
            ProcesarEventosResponse response = useCase.procesarPendientes(new ProcesarEventosPendientesCommand(50));
            if (response.total() > 0) {
                log.info("Lote Outbox procesado: total={}, procesados={}, fallidos={}",
                        response.total(), response.procesados(), response.fallidos());
            }
        } catch (Exception e) {
            log.error("Error inesperado durante la ejecución programada de OutboxEventPoller: {}", e.getMessage(), e);
        }
    }
}
