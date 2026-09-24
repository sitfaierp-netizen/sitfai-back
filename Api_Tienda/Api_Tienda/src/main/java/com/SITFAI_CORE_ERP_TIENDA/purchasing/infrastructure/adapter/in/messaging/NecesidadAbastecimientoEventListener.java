package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearSolicitudAutomaticaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.CrearSolicitudAutomaticaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.event.NecesidadAbastecimientoDetectadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Oído del Bounded Context Purchasing (Driving Adapter — Messaging).
 * <p>
 * Escucha el {@link NecesidadAbastecimientoDetectadaEvent} emitido asíncronamente
 * por el módulo {@code replenishment} cuando un producto perfora su Punto de Reorden.
 * Al recibirlo, mapea los datos al {@link CrearSolicitudAutomaticaCommand} y delega
 * la ejecución limpiamente al caso de uso de Aplicación.
 * <p>
 * Regla Protocolo-5 (MCP § 5): Comunicación inter-módulos EXCLUSIVAMENTE por Domain Events.
 * Los bounded contexts NO se llaman directamente ni comparten repositorios.
 * Regla REGLA-1: Este componente vive en Infraestructura. El Dominio de Purchasing
 * NO conoce nada del módulo {@code replenishment}.
 * Regla MT-01: El {@code empresaId} proviene del evento (originado en el JWT), no de ningún payload.
 */
@Component
public class NecesidadAbastecimientoEventListener {

    private static final Logger log = LoggerFactory.getLogger(NecesidadAbastecimientoEventListener.class);

    private final CrearSolicitudAutomaticaUseCase crearSolicitudAutomaticaUseCase;

    public NecesidadAbastecimientoEventListener(CrearSolicitudAutomaticaUseCase crearSolicitudAutomaticaUseCase) {
        this.crearSolicitudAutomaticaUseCase = crearSolicitudAutomaticaUseCase;
    }

    /**
     * Intercepta la señal de necesidad de reabastecimiento y orquesta la creación
     * automática de una Solicitud de Abastecimiento en el módulo Purchasing.
     *
     * @param event Evento emitido por el motor de reposición automática (replenishment).
     */
    @Async
    @EventListener
    public void onNecesidadDetectada(NecesidadAbastecimientoDetectadaEvent event) {
        log.info("Purchasing Oído: NecesidadAbastecimientoDetectadaEvent recibido. " +
                 "eventId={}, empresa={}, bodega={}, producto={}, cantidadAReponer={}",
                 event.id(), event.empresaId(), event.bodegaId(),
                 event.productoId(), event.cantidadAReponer());

        // Mapeo del evento de dominio de replenishment → Command de la capa de Aplicación
        // Protocolo-5: El adaptador es responsable del mapeo; el caso de uso recibe solo el Command.
        CrearSolicitudAutomaticaCommand command = new CrearSolicitudAutomaticaCommand(
                event.empresaId(),
                event.bodegaId(),
                event.productoId(),
                BigDecimal.valueOf(event.cantidadAReponer())
        );

        try {
            var response = crearSolicitudAutomaticaUseCase.crearAutomatica(command);
            log.info("Purchasing Oído: SolicitudAbastecimiento creada exitosamente. " +
                     "solicitudId={}, estado={}",
                     response.id(), response.estado());
        } catch (Exception e) {
            log.error("Purchasing Oído: Error al crear SolicitudAbastecimiento automática. " +
                      "empresa={}, bodega={}, producto={}: {}",
                      event.empresaId(), event.bodegaId(), event.productoId(), e.getMessage(), e);
            // No relanzar — el listener asíncrono nunca debe romper el contexto del emisor.
        }
    }
}
